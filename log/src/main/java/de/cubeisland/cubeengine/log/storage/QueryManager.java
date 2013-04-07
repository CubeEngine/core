package de.cubeisland.cubeengine.log.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.ActionType;

import gnu.trove.map.hash.THashMap;

public class QueryManager
{
    private final Database database;
    private final Log module;

    private final ExecutorService storeExecutor;
    private final Runnable storeRunner;
    private final ExecutorService lookupExecutor;
    private final Runnable lookupRunner;

    Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<QueuedLog>();
    Queue<QueuedSqlParams> queuedLookups = new ConcurrentLinkedQueue<QueuedSqlParams>();

    private final int batchSize;
    private Future<?> futureStore = null;
    private Future<?> futureLookup = null;

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
            String sql = builder.createTable("log_actiontypes",true).beginFields()
                .field("id",AttrType.INT,true).autoIncrement()
                .field("name", AttrType.VARCHAR, 32)
                .unique("name")
                .primaryKey("id")
                .endFields()
                .engine("innoDB").defaultcharset("utf8")
                .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_actiontypes")
                         .cols("name")
                         .end().end();
            this.database.storeStatement(this.getClass(), "registerAction", sql);
            sql = builder.deleteFrom("log_actiontypes")
                         .where().field("name").isEqual().value()
                         .end().end();
            this.database.storeStatement(this.getClass(), "unregisterAction", sql);
            sql = builder.select().wildcard().from("log_actiontypes").end().end();
            this.database.storeStatement(this.getClass(), "getAllActions", sql);

            sql = builder.createTable("log_entries", true).beginFields()
                                .field("id", AttrType.INT, true).autoIncrement()
                                .field("date", AttrType.DATETIME)
                                .field("world", AttrType.INT, true, false)
                                .field("x", AttrType.INT, false, false)
                                .field("y", AttrType.INT, false, false)
                                .field("z", AttrType.INT, false, false)
                                .field("action", AttrType.INT, true)
                                .field("causer", AttrType.BIGINT, false, false)
                                .field("block",AttrType.VARCHAR, 255, false)
                .field("data",AttrType.BIGINT,false,false) // in kill logs this is the killed entity
                .field("newBlock", AttrType.VARCHAR, 255, false)
                .field("newData",AttrType.TINYINT, false,false)
                .field("additionalData",AttrType.VARCHAR,255, false)
                .foreignKey("world").references("worlds", "key")
                .foreignKey("action").references("log_actiontypes","id").onDelete("CASCADE")
                .index("x","y","z","world","date")
                .index("causer")
                .index("block")
                .index("newBlock")
                .primaryKey("id").endFields()
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

        this.storeRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    doEmptyLogs(batchSize);
                } catch (Exception ex) {
                    QueryManager.this.module.getLog().log(LogLevel.ERROR, "Error while logging!", ex);
                }
            }
        };
        this.storeExecutor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory()); //TODO shut down
        this.lookupRunner = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    doQueryLookup();
                } catch (Exception ex) {
                    QueryManager.this.module.getLog().log(LogLevel.ERROR, "Error while lookup!", ex);
                }
            }
        };
        this.lookupExecutor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory()); //TODO shut down
    }

    private void doQueryLookup()
    {
        try
        {
            if (queuedLookups.isEmpty())
            {
                return;
            }
            QueuedSqlParams poll = this.queuedLookups.poll();
            final Lookup lookup = poll.lookup;
            final User user = poll.user;
            PreparedStatement stmt = this.database.prepareStatement(poll.sql);
            for (int i = 0 ; i <  poll.sqlData.size() ; ++i)
            {
                stmt.setObject(i+1, poll.sqlData.get(i));
            }
            System.out.print(stmt); //TODO remove
            ResultSet resultSet = stmt.executeQuery();
            QueryResults results = new QueryResults();
            while (resultSet.next())
            {
                long entryID = resultSet.getLong("id");
                Timestamp timestamp = resultSet.getTimestamp("date");
                int action = resultSet.getInt("action");
                long worldId = resultSet.getLong("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                long causer = resultSet.getLong("causer");
                String block = resultSet.getString("block");
                long data = resultSet.getLong("data");
                String newBlock = resultSet.getString("newBlock");
                int newData = resultSet.getInt("newData");
                String additionalData = resultSet.getString("additionalData");
                LogEntry logEntry = new LogEntry(module,entryID,timestamp,action,worldId,x,y,z,causer,block,data,newBlock,newData,additionalData);
                results.addResult(logEntry);
            }
            lookup.setQueryResults(results);
            if (user != null && user.isOnline())
            {
                module.getCore().getTaskManager().scheduleSyncDelayedTask(module,
                  new Runnable()
                  {
                      @Override
                      public void run()
                      {
                          lookup.show(user);
                      }
                  });
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while getting logs from database!", e);
        }
        if (!queuedLookups.isEmpty())
        {
            this.futureLookup = this.lookupExecutor.submit(this.lookupRunner);
        }
    }

    private void doEmptyLogs(int amount)
    {
        try
        {
            if (queuedLogs.isEmpty())
            {
                return;
            }
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
            this.database.getConnection().setAutoCommit(false);
            PreparedStatement stmt = this.database.getStoredStatement(this.getClass(),"storeLog");
            try
            {
                for (QueuedLog log : logs)
                {
                    log.addDataToBatch(stmt);
                }
                stmt.executeBatch();
                this.database.getConnection().commit();
            }
            catch (SQLException ex)
            {
                throw new StorageException("Error while storing log-entries", ex, stmt);
            }
            finally
            {
                this.database.getConnection().setAutoCommit(true);
            }
            long nanos = Profiler.endProfiling("logging");
            timeSpend += nanos;
            logsLogged += logSize;
            if (logSize == batchSize)
            {
                timeSpendFullLoad += nanos;
                logsLoggedFullLoad += logSize;
            }
            if (logSize > 50)
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
                this.futureStore = this.storeExecutor.submit(this.storeRunner);
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

    protected void queueLog(QueuedLog log)
    {
        this.queuedLogs.offer(log);
        if (this.futureStore == null || this.futureStore.isDone())
        {
            this.futureStore = storeExecutor.submit(storeRunner);
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

    public void prepareLookupQuery(final Lookup lookup, final User user)
    {
        final QueryParameter params = lookup.getQueryParameter();
        SelectBuilder selectBuilder =
            this.database.getQueryBuilder().select("id","date","action",
                                                   "world","x","y","z","causer",
                                                   "block","data","newBlock","newData",
                                                   "additionalData")
                         .from("log_entries").where();
        boolean needAnd = false;
        ArrayList<Object> dataToInsert = new ArrayList<Object>();
        if (!params.actions.isEmpty())
        {
            selectBuilder.beginSub().field("action");
            if (!params.includeActions)
            {
                selectBuilder.not();
            }
            selectBuilder.in().valuesInBrackets(params.actions.size()).endSub();
            for (ActionType type : params.actions)
            {
                dataToInsert.add(type.getID());
            }
            needAnd = true;
        }
        if (params.hasTime()) // has since / before / from-to
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            selectBuilder.beginSub();
            Long from_since = params.from_since;
            Long to_before = params.to_before;
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
        if (params.worldID != null) // has world
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            selectBuilder.beginSub().field("world").isEqual().value(params.worldID);
            if (params.location1 != null)
            {
                BlockVector3 loc1 = params.location1;
                if (params.location2 != null)// has area
                {
                    BlockVector3 loc2 = params.location2;
                    selectBuilder.and().beginSub()
                        .field("x").between(loc1.x,loc2.x)
                        .and().field("y").between(loc1.y,loc2.y)
                        .and().field("z").between(loc1.z,loc2.z)
                        .endSub();
                }
                else if (params.radius == null)// has single location
                {
                    selectBuilder.and().beginSub()
                         .field("x").isEqual().value(loc1.x)
                         .and().field("y").isEqual().value(loc1.y)
                         .and().field("z").isEqual().value(loc1.z)
                         .endSub();
                }
                else
                {
                    selectBuilder.and().beginSub()
                                 .field("x").between(loc1.x-params.radius,loc1.x+params.radius)
                                 .and().field("y").between(loc1.y-params.radius,loc1.y+params.radius)
                                 .and().field("z").between(loc1.z-params.radius,loc1.z+params.radius)
                                 .endSub();
                }
            }
            selectBuilder.endSub();
            needAnd = true;
        }
        String sql = selectBuilder.end().end();
        System.out.print(user.getName() + ": Lookup queued!");
        this.queuedLookups.offer(new QueuedSqlParams(lookup,user,sql,dataToInsert));
        if (this.futureLookup == null || this.futureLookup.isDone())
        {
            this.futureLookup = lookupExecutor.submit(lookupRunner);
        }
    }

    public Map<String,Long> getActionTypesFromDatabase()
    {
        try
        {
            Map<String,Long> map = new THashMap<String, Long>();
            ResultSet resultSet = this.database.preparedQuery(this.getClass(),"getAllActions");
            while (resultSet.next())
            {
                map.put(resultSet.getString("name"),resultSet.getLong("id"));
            }
            return map;
        }
        catch (SQLException e)
        {
            throw new StorageException("Could not get actionTypes from db!",e,this.database.getStoredStatement(this.getClass(),"getAllActions"));
        }
    }

    public long registerActionType(String name)
    {
        try
        {
            return (Long)this.database.getLastInsertedId(this.getClass(),"registerAction",name);
        }
        catch (SQLException e)
        {
            throw new StorageException("Could not get register ActionType!",e,this.database.getStoredStatement(this.getClass(),"registerAction"));
        }
    }

    public void unregisterActionType(String name)
    {
        try
        {
           this.database.preparedExecute(this.getClass(),"unregisterAction",name);
        }
        catch (SQLException e)
        {
            throw new StorageException("Could not get unregister ActionType!",e,this.database.getStoredStatement(this.getClass(),"unregisterAction"));
        }
    }

    public static class QueuedSqlParams
    {
        public final Lookup lookup;
        public final String sql;
        public final ArrayList sqlData;
        private final User user;

        public QueuedSqlParams(Lookup lookup, User user, String sql, ArrayList sqlData)
        {
            this.lookup = lookup;
            this.sql = sql;
            this.sqlData = sqlData;
            this.user = user;
        }


    }
}
