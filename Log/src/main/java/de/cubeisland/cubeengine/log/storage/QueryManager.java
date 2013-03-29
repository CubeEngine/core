package de.cubeisland.cubeengine.log.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
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
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.util.Profiler;
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
        //TODO fillLookup
        lookup.clear();
        return lookup;
    }
}
