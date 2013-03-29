package de.cubeisland.cubeengine.log.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.core.util.worker.AsyncTaskQueue;
import de.cubeisland.cubeengine.log.Log;

public class QueryManager
{
    private final ExecutorService executorService;
    private final AsyncTaskQueue taskQueue;
    private final Database database;
    private final Log module;

    private final ExecutorService executor;
    private final Runnable runner;

    Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<QueuedLog>();
    private volatile boolean running = false;

    private final int batchSize;
    private Future<?> future = null;

    private long timeSpend = 0;
    private long logsLogged = 1;

    private long timeSpendFullLoad = 0;
    private long logsLoggedFullLoad = 1;

    private CountDownLatch latch = null;


    public QueryManager(Log module)
    {

        this.database = module.getCore().getDB();


        this.module = module;
        this.batchSize = module.getConfiguration().loggingBatchSize;

        try
        {
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("log_entries", true).beginFields()
                                .field("key", AttrType.INT, true).autoIncrement()
                                .field("date", AttrType.TIMESTAMP)
                                .field("action", AttrType.TINYINT, true)
                                .field("world", AttrType.INT, true, false)
                                .field("x", AttrType.INT, false, false)
                                .field("y", AttrType.INT, false, false)
                                .field("z", AttrType.INT, false, false)
                                .field("causer", AttrType.BIGINT, false, false)
                                .field("block",AttrType.VARCHAR, 255, false)
                .field("data",AttrType.BIGINT,false,false) // in kill logs this is the killed entity
                .field("newBlock", AttrType.VARCHAR, 255, false)
                .field("newData",AttrType.TINYINT, false,false)
                .field("additionalData",AttrType.VARCHAR,255, false)
                .foreignKey("world").references("worlds", "key")
                .index("x")
                .index("y")
                .index("z")
                .index("action")
                .index("causer")
                .index("block")
                .index("newBlock")
                .primaryKey("key").endFields()
                .engine("innoDB").defaultcharset("utf8")
                .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_entries")
                         .cols("date", "action", "world", "x", "y", "z", "causer",
                               "block", "data", "newBlock", "newData", "additionalData")
                         .end().end();
            this.database.storeStatement(this.getClass(), "storeLog", sql);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error during initialization of log-tables", ex);
        }

        this.executorService = Executors
            .newSingleThreadScheduledExecutor(this.module.getCore().getTaskManager().getThreadFactory()); // TODO is not shut down!
        this.taskQueue = new AsyncTaskQueue(this.executorService); // TODO is not shut down!

        runner = new Runnable() {
            @Override
            public void run() {
                taskQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doEmptyLogs(batchSize);
                        } catch (Exception ex) {
                            QueryManager.this.module.getLog().log(LogLevel.ERROR, "Error while logging!", ex);
                        }
                    }
                });
            }
        };
        executor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory());
    }

    private void doEmptyLogs(int amount)
    {
        try
        {
            if (queuedLogs.isEmpty())
            {
                return;
            }
            if (running)
            {
                return;
            }
            running = true;
            final Queue<QueuedLog> logs = new LinkedList<QueuedLog>();
            for (int i = 0; i < amount; i++) // log <amount> next logs...
            {
                QueuedLog toLog = this.queuedLogs.poll();
                if (toLog == null)
                {
                    break;
                }
                logs.offer(toLog);
            }
            Profiler.startProfiling("logging");
            int logSize = logs.size();
            PreparedStatement stmt = this.database.getStoredStatement(this.getClass(),"storeLog");
            try
            {
                this.database.getConnection().setAutoCommit(false);
                for (QueuedLog log : logs)
                {
                    log.addDataToBatch(stmt);
                }
                stmt.executeBatch();
                this.database.getConnection().commit();
                this.database.getConnection().setAutoCommit(true);
            }
            catch (SQLException ex)
            {
                throw new StorageException("Error while storing log-entries", ex, stmt);
            }
            finally
            {
                running = false;
            }
            long nanos = Profiler.endProfiling("logging");
            timeSpend += nanos;
            logsLogged += logSize;
            if (logSize == batchSize)
            {
                timeSpendFullLoad += nanos;
                logsLoggedFullLoad += logSize;
            }
            if (logSize > 20)
            {
                this.module.getLog().log(LogLevel.DEBUG,
                                         logSize + " logged in: " + TimeUnit.NANOSECONDS.toMillis(nanos) +
                                             "ms | remaining logs: " + queuedLogs.size());
                this.module.getLog().log(LogLevel.DEBUG,
                                         "Average logtime per log: " + TimeUnit.NANOSECONDS.toMicros(timeSpend / logsLogged)+ " micros");
                this.module.getLog().log(LogLevel.DEBUG,
                                         "Average logtime per log in full load: " + TimeUnit.NANOSECONDS.toMicros(timeSpendFullLoad / logsLoggedFullLoad)+" micros");
            }
            if (!queuedLogs.isEmpty())
            {
                this.future = this.executor.submit(this.runner);
            }
            else if (this.latch != null)
            {
                this.latch.countDown();
            }
        }
        catch (Exception ex)
        {
            Profiler.endProfiling("logging"); // end profiling so we can start again later
            throw new IllegalStateException("Error while logging", ex);
        }
    }

    protected void queueLog(Timestamp timestamp, Long worldID, Integer x, Integer y, Integer z, ActionType action, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        this.queuedLogs.offer(new QueuedLog(timestamp,worldID,x,y,z,action.value,causer,block,data,newBlock,newData,additionalData));
        if (this.future == null || this.future.isDone())
        {
            this.future = executor.submit(runner);
        }
    }

    protected void disable()
    {
        if (!this.queuedLogs.isEmpty())
        {
            latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                this.module.getLog().log(LogLevel.WARNING,"Error while waiting!",e);
            }
        }
    }

    public Lookup fillLookup(Lookup lookup)
    {
        lookup.clear();
        SelectBuilder selectBuilder = this.database.getQueryBuilder().select().wildcard().from("log_entries").where();
        boolean needAnd = false;
        ArrayList<Object> dataToInsert = new ArrayList<Object>();
        if (!lookup.getActions().isEmpty())
        {
            selectBuilder.beginSub().field("action");
            if (!lookup.hasIncludeActions())
            {
                selectBuilder.not();
            }
            selectBuilder.in().valuesInBrackets(lookup.getActions().size()).endSub(); // TODO replace ? with actions later
            for (ActionType type : lookup.getActions())
            {
                dataToInsert.add(type.value);
            }
            needAnd = true;
        }
        if (lookup.hasTime()) // has since / before / from-to
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            selectBuilder.beginSub();
            Long from_since = lookup.getFromSince();
            Long to_before = lookup.getToBefore();
            if (from_since == null) // before
            {
                selectBuilder.field("date").is(ComponentBuilder.LESS).value();
                dataToInsert.add(new Timestamp(to_before));
            }
            else if (to_before == null) // since
            {
                selectBuilder.field("date").is(ComponentBuilder.GREATER).value();
                dataToInsert.add(new Timestamp(from_since));
            }
            else // from - to
            {
                selectBuilder.field("date").between();
                dataToInsert.add(new Timestamp(from_since));
                dataToInsert.add(new Timestamp(to_before));
            }
            selectBuilder.endSub();
            needAnd = true;
        }
        if (lookup.getWorld() != null) // has world
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            selectBuilder.beginSub().field("world").isEqual().value(lookup.getWorld());
            if (lookup.getLocation1() != null)
            {
                BlockVector3 loc1 = lookup.getLocation1();
                if (lookup.getLocation2() != null)// has area
                {
                    BlockVector3 loc2 = lookup.getLocation2();
                    selectBuilder.and().beginSub()
                        .field("x").between(loc1.x,loc2.x)
                        .and().field("y").between(loc1.y,loc2.y)
                        .and().field("z").between(loc1.z,loc2.z)
                        .endSub();
                }
                else // has single location
                {
                    selectBuilder.and().beginSub()
                         .field("x").isEqual().value(loc1.x)
                         .and().field("y").isEqual().value(loc1.y)
                         .and().field("z").isEqual().value(loc1.z)
                         .endSub();
                }
            }
            selectBuilder.endSub();
            needAnd = true;
        }
        String sql = selectBuilder.end().end();
        System.out.print("\n"+sql);
        try
        {
            PreparedStatement stmt = this.database.prepareStatement(sql);
            for (int i = 0 ; i < dataToInsert.size() ; ++i)
            {
                stmt.setObject(i+1, dataToInsert.get(i));
            }
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next())
            {
                long key = resultSet.getLong("key");
                Timestamp timestamp = resultSet.getTimestamp("date");
                int action = resultSet.getInt("action");
                long worldId = resultSet.getLong("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                long causer = resultSet.getLong("causer");
                String block = resultSet.getString("block");
                Long data = resultSet.getLong("data");
                String newBlock = resultSet.getString("newBlock");
                Integer newData = resultSet.getInt("newData");
                String additionalData = resultSet.getString("additionalData");

                System.out.print(key + " " + timestamp + " A"+action+": W"+worldId +"("+x+","+y+","+z+") "
                                 + "C:"+causer+ " B:"+block+":"+data+"->NB:"+newBlock+":"+newData+ " A:"+additionalData);
            }
            //TODO fillLookup
            return lookup;
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while creating prepared statement for log-query!", e);
        }
    }
}
