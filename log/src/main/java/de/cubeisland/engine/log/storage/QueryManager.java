/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.ActionType;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.log.storage.TableActionTypes.TABLE_ACTION_TYPE;
import static de.cubeisland.engine.log.storage.TableLogEntry.TABLE_LOG_ENTRY;

public class QueryManager
{
    private final Log module;

    private final ExecutorService storeExecutor;
    private final Runnable storeRunner;
    private final ExecutorService lookupExecutor;
    private final Runnable lookupRunner;

    Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<>();
    Queue<QueuedSqlParams> queuedLookups = new ConcurrentLinkedQueue<>();

    private int batchSize;
    private Future<?> futureStore = null;
    private Future<?> futureLookup = null;

    private long timeSpend = 0;
    private long logsLogged = 1;

    private long timeSpendFullLoad = 0;
    private long logsLoggedFullLoad = 1;

    private CountDownLatch latch = new CountDownLatch(0);
    private CountDownLatch shutDownLatch = new CountDownLatch(0);

    private int cleanUpTaskId;

    private Connection insertConnection = null;
    private DSLContext dsl;
    private DSLContext cleanUpDsl;
    private Database database;

    private TableLogEntry CURRENT_TABLE;
    private TableLogEntry OPTIMIZE_TABLE;

    public QueryManager(Log module)
    {
        this.CURRENT_TABLE = TABLE_LOG_ENTRY;
        this.database = module.getCore().getDB();
        this.dsl = this.database.getDSL();
        this.cleanUpDsl = this.database.getDSL();
        this.module = module;
        this.batchSize = module.getConfiguration().loggingBatchSize;

        this.storeRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    doEmptyLogs(batchSize);
                } catch (Exception ex) {
                    QueryManager.this.module.getLog().error("Error while logging!", ex);
                }
            }
        };
        this.storeExecutor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory());
        this.lookupRunner = new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    doQueryLookup();
                } catch (Exception ex) {
                    QueryManager.this.module.getLog().error("Error while lookup!", ex);
                }
            }
        };
        this.lookupExecutor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory());

        this.cleanUpTaskId = this.module.getCore().getTaskManager().runAsynchronousTimer(this.module, new Runnable()
        {
            @Override
            public void run()
            {
                cleanUpLogs();
            }
        }, this.module.getConfiguration().cleanUpDelay.toTicks(), this.module.getConfiguration().cleanUpDelay.toTicks());
    }

    /**
     * Cannot be executed in the main-thread!
     */
    private void optimizeTable()
    {
        if (CubeEngine.isMainThread()) throw new IllegalStateException("ONLY use Asynchronously!");
        try
        {
            this.module.getLog().debug("Analyze - Step 1/6: Wait for inserts to finish then pause inserts");
            latch.await(); // Wait for batch insert to finish!
            latch = new CountDownLatch(1);
        }
        catch (InterruptedException e)
        {
            this.module.getLog().error("Could not start optimizing!", e);
        }
        this.module.getLog().debug("Analyze - Step 2/6: Create temporary table and swap tables");
        TableLogEntry temporaryTable = TableLogEntry.initTempTable(this.database);
        this.OPTIMIZE_TABLE = this.CURRENT_TABLE;
        this.CURRENT_TABLE = temporaryTable;
        this.module.getLog().debug("Analyze - Step 3/6: Optimize Table and continue logging into temporary table");
        latch.countDown();
        this.cleanUpDsl.execute("ANALYZE TABLE " + OPTIMIZE_TABLE.getName());
        try
        {
            this.module.getLog().debug("Analyze - Step 4/6: Wait for inserts to finish then pause inserts");
            latch.await(); // Wait for batch insert to finish!
            latch = new CountDownLatch(1); // Block normal inserts
        }
        catch (InterruptedException e)
        {
            this.module.getLog().error("Could not start copying back to optimized table!", e);
        }
        this.module.getLog().debug("Analyze - Step 5/6: Insert data from temporary table into the optimized table and drop temporary table");
        this.cleanUpDsl.insertInto(OPTIMIZE_TABLE, OPTIMIZE_TABLE.DATE, OPTIMIZE_TABLE.WORLD, OPTIMIZE_TABLE.X, OPTIMIZE_TABLE.Y, OPTIMIZE_TABLE.Z,
                                   OPTIMIZE_TABLE.ACTION, OPTIMIZE_TABLE.CAUSER,OPTIMIZE_TABLE.BLOCK, OPTIMIZE_TABLE.DATA,
                                   OPTIMIZE_TABLE.NEWBLOCK, OPTIMIZE_TABLE.NEWDATA, OPTIMIZE_TABLE.ADDITIONALDATA).
                           select(this.cleanUpDsl.select(CURRENT_TABLE.DATE, CURRENT_TABLE.WORLD, CURRENT_TABLE.X, CURRENT_TABLE.Y, CURRENT_TABLE.Z,
                                                         CURRENT_TABLE.ACTION, CURRENT_TABLE.CAUSER,CURRENT_TABLE.BLOCK, CURRENT_TABLE.DATA,
                                                         CURRENT_TABLE.NEWBLOCK, CURRENT_TABLE.NEWDATA, CURRENT_TABLE.ADDITIONALDATA).from(CURRENT_TABLE)).execute();
        this.dsl.execute("DROP TABLE " + temporaryTable.getName());
        this.module.getLog().debug("Analyze - Step 6/6: Return back to normal logging. Table optimized!");
        this.CURRENT_TABLE = this.OPTIMIZE_TABLE;
        latch.countDown(); // Start normal logging again

    }

    private boolean optimizeRunning = false;

    /**
     * Analyse the indices of the log table
     */
    public void analyze()
    {
        if (optimizeRunning) return;
        this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
        {
            @Override
            public void run()
            {
                optimizeTable();
                optimizeRunning = false;
            }
        }).start();
    }

    private void cleanUpLogs()
    {
        try
        {
            this.module.getLog().debug("CleanUp - Step 1/7: Wait for inserts to finish then pause inserts");
            latch.await(); // Wait for batch insert to finish!
            latch = new CountDownLatch(1);
        }
        catch (InterruptedException e)
        {
            this.module.getLog().error("Could not start optimizing!", e);
        }
        this.module.getLog().debug("CleanUp - Step 2/7: Create temporary table and swap tables");
        TableLogEntry temporaryTable = TableLogEntry.initTempTable(this.database);
        this.OPTIMIZE_TABLE = this.CURRENT_TABLE;
        this.CURRENT_TABLE = temporaryTable;
        this.module.getLog().debug("CleanUp - Step 3/7: CleanUp of deleted worlds");
        latch.countDown();
        if (this.module.getConfiguration().cleanUpDeletedWorlds)
        {
            long[] worlds = this.module.getCore().getWorldManager().getAllWorldIds();
            final UInteger[] values = new UInteger[worlds.length];
            for (int i = 0 ; i < worlds.length; i++)
            {
                values[i] = UInteger.valueOf(worlds[i]);
            }
            cleanUpDsl.delete(TABLE_LOG_ENTRY).where(TABLE_LOG_ENTRY.WORLD.notIn(values)).execute();
        }
        this.module.getLog().debug("CleanUp - Step 4/7: CleanUp of old logs");
        if (this.module.getConfiguration().cleanUpOldLogs)
        {
            cleanUpDsl.delete(TABLE_LOG_ENTRY).where(TABLE_LOG_ENTRY.DATE.le(new Timestamp(System
                                                                                               .currentTimeMillis() - module
                .getConfiguration().cleanUpOldLogsTime.toMillis()))).execute();
        }
        try
        {
            this.module.getLog().debug("CleanUp - Step 5/7: Wait for inserts to finish then pause inserts");
            latch.await(); // Wait for batch insert to finish!
            latch = new CountDownLatch(1); // Block normal inserts
        }
        catch (InterruptedException e)
        {
            this.module.getLog().error("Could not start copying back to clean table!", e);
        }
        this.module.getLog().debug("Optimize - Step 6/7: Insert data from temporary table into the optimized table and drop temporary table");
        this.cleanUpDsl.insertInto(OPTIMIZE_TABLE, OPTIMIZE_TABLE.DATE, OPTIMIZE_TABLE.WORLD, OPTIMIZE_TABLE.X, OPTIMIZE_TABLE.Y, OPTIMIZE_TABLE.Z,
                                   OPTIMIZE_TABLE.ACTION, OPTIMIZE_TABLE.CAUSER,OPTIMIZE_TABLE.BLOCK, OPTIMIZE_TABLE.DATA,
                                   OPTIMIZE_TABLE.NEWBLOCK, OPTIMIZE_TABLE.NEWDATA, OPTIMIZE_TABLE.ADDITIONALDATA).
                           select(this.cleanUpDsl.select(CURRENT_TABLE.DATE, CURRENT_TABLE.WORLD, CURRENT_TABLE.X, CURRENT_TABLE.Y, CURRENT_TABLE.Z,
                                                         CURRENT_TABLE.ACTION, CURRENT_TABLE.CAUSER,CURRENT_TABLE.BLOCK, CURRENT_TABLE.DATA,
                                                         CURRENT_TABLE.NEWBLOCK, CURRENT_TABLE.NEWDATA, CURRENT_TABLE.ADDITIONALDATA).from(CURRENT_TABLE)).execute();
        this.dsl.execute("DROP TABLE " + temporaryTable.getName());
        this.module.getLog().debug("Optimize - Step 7/7: Re-Analyze Indices and then return back to normal logging. Table cleaned up!");
        this.CURRENT_TABLE = this.OPTIMIZE_TABLE;
        latch.countDown(); // Start normal logging again
        this.analyze();
    }

    private void doQueryLookup()
    {
        if (queuedLookups.isEmpty())
        {
            return;
        }
        QueuedSqlParams poll = this.queuedLookups.poll();
        final QueryAction queryAction = poll.action;
        final Lookup lookup = poll.lookup;
        final User user = poll.user;
        Result<LogEntry> entries = poll.query.limit(10000).fetch();
        QueryResults results = new QueryResults(lookup);
        for (LogEntry entry : entries)
        {
            results.addResult(entry.init(module));
        }
        lookup.setQueryResults(results);
        if (user != null && user.isOnline())
        {
            module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    switch (queryAction)
                    {
                        case SHOW:
                            lookup.show(user);
                               return;
                        case ROLLBACK:
                            lookup.rollback(user, false);
                            return;
                        case ROLLBACK_PREVIEW:
                            lookup.rollback(user, true);
                            return;
                        case REDO:
                            user.sendMessage("REDO is not finished yet"); // TODO
                            return;
                        case REDO_PREVIEW:
                            user.sendMessage("REDO_PREVIEW is not finished yet"); // TODO
                    }
                }
            });
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
            this.latch.await(); // Wait if still doing inserts
            this.latch = new CountDownLatch(1);
            if (queuedLogs.isEmpty())
            {
                return;
            }
            final Queue<QueuedLog> logs = new LinkedList<>();
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
            // --- SQL ---
            String sql = dsl
                .insertInto(CURRENT_TABLE, CURRENT_TABLE.DATE, CURRENT_TABLE.ACTION, CURRENT_TABLE.WORLD,
                            CURRENT_TABLE.X, CURRENT_TABLE.Y, CURRENT_TABLE.Z, CURRENT_TABLE.CAUSER,
                            CURRENT_TABLE.BLOCK, CURRENT_TABLE.DATA, CURRENT_TABLE.NEWBLOCK, CURRENT_TABLE.NEWDATA, CURRENT_TABLE.ADDITIONALDATA)
                .values((Timestamp)null, null, null, null, null, null, null, null, null, null, null, null).getSQL();
            if (this.insertConnection == null || this.insertConnection.isClosed())
            {
                this.insertConnection = this.database.getConnection();
                this.insertConnection.setAutoCommit(false);
            }
            PreparedStatement statement = this.insertConnection.prepareStatement(sql);
            for (QueuedLog log : logs)
            {
                log.bindTo(statement);
            }
            statement.executeBatch();
            this.insertConnection.commit();
            // --- --- ---
            long nanos = Profiler.endProfiling("logging");
            timeSpend += nanos;
            logsLogged += logSize;
            if (logSize == batchSize)
            {
                timeSpendFullLoad += nanos;
                logsLoggedFullLoad += logSize;
            }
            if (logSize > this.module.getConfiguration().showLogInfoInConsole)
            {
                this.module.getLog().debug("{} logged in: {} ms | remaining logs: {} | AVG/AVG-FULL {} / {} micros",
                                         logSize, TimeUnit.NANOSECONDS.toMillis(nanos), queuedLogs.size(),
                                         TimeUnit.NANOSECONDS.toMicros(timeSpend / logsLogged),
                            TimeUnit.NANOSECONDS.toMicros(timeSpendFullLoad / logsLoggedFullLoad));
            }
            latch.countDown();
            if (!queuedLogs.isEmpty())
            {
                this.futureStore = this.storeExecutor.submit(this.storeRunner);
            }
            else
            {
                this.insertConnection.setAutoCommit(true);
                this.insertConnection.close();
                this.insertConnection = null;
                this.shutDownLatch.countDown();
            }
        }
        catch (Exception ex)
        {
            Profiler.endProfiling("logging"); // end profiling so we can start again later
            latch = new CountDownLatch(0); // and reset latch
            module.getLog().error("Error while logging!", ex);
            ex.printStackTrace();
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
        this.module.getCore().getTaskManager().cancelTask(this.module, cleanUpTaskId);
        if (!this.queuedLogs.isEmpty())
        {
            this.lookupExecutor.shutdown();
            this.batchSize = this.batchSize * 5;
            this.awaitPendingInserts(this.queuedLogs.size());
            this.storeExecutor.shutdown();
        }
    }

    private void awaitPendingInserts(final int size)
    {
        this.module.getLog().info("Waiting for {} more logs to be logged!", size);
        this.shutDownLatch = new CountDownLatch(1);
        try {
            shutDownLatch.await(1, TimeUnit.MINUTES); // wait for all logs to be logged (stop after 3 min)
        }
        catch (InterruptedException e)
        {
            this.module.getLog().warn("Error while waiting! " + e.getLocalizedMessage(), e);
            return;
        }
        if (this.queuedLogs.size() != 0)
        {
            if (this.queuedLogs.size() < size) // Wait another 3 min
            {
                awaitPendingInserts(this.queuedLogs.size());
            }
            else
            {
                this.module.getLog().warn("Logging doesn't seem to progress! Aborting...", new Exception());
            }
        }
    }

    public enum QueryAction
    {
        SHOW, ROLLBACK, REDO, ROLLBACK_PREVIEW, REDO_PREVIEW;
    }

    public void prepareLookupQuery(final Lookup lookup, final User user, QueryAction action)
    {
        final QueryParameter params = lookup.getQueryParameter();
        SelectWhereStep<LogEntry> whereStep = this.dsl.selectFrom(TABLE_LOG_ENTRY);
        ArrayList<Condition> conditions = new ArrayList<>();
        if (!params.actions.isEmpty())
        {
            boolean include = params.includeActions();
            Collection<UInteger> actions = new HashSet<>();
            for (Entry<ActionType, Boolean> entry : params.actions.entrySet())
            {
                if (!include || entry.getValue())
                {
                    actions.add(entry.getKey().getModel().getId());
                }
            }
            if (!include)
            {
                conditions.add(TABLE_LOG_ENTRY.ACTION.notIn(actions));
            }
            else
            {
                conditions.add(TABLE_LOG_ENTRY.ACTION.in(actions));
            }
        }
        if (params.hasTime()) // has since / before / from-to
        {
            if (params.from_since == null) // before
            {
                conditions.add(TABLE_LOG_ENTRY.DATE.le(new Timestamp(params.to_before)));
            }
            else if (params.to_before == null) // since
            {
                conditions.add(TABLE_LOG_ENTRY.DATE.greaterThan(new Timestamp(params.from_since)));
            }
            else // from - to
            {
                conditions.add(TABLE_LOG_ENTRY.DATE.between(new Timestamp(params.from_since), new Timestamp(params.to_before)));
            }
        }
        if (params.worldID != null) // has world
        {
            conditions.add(TABLE_LOG_ENTRY.WORLD.eq(UInteger.valueOf(params.worldID)));
            if (params.location1 != null)
            {
                BlockVector3 loc1 = params.location1;
                if (params.location2 != null)// has area
                {
                    BlockVector3 loc2 = params.location2;
                    boolean locX = loc1.x < loc2.x;
                    boolean locY = loc1.y < loc2.y;
                    boolean locZ = loc1.z < loc2.z;
                    conditions.add(TABLE_LOG_ENTRY.X.between(locX ? loc1.x : loc2.x, locX ? loc2.x : loc1.x));
                    conditions.add(TABLE_LOG_ENTRY.Y.between(locY ? loc1.y : loc2.y, locY ? loc2.y : loc1.y));
                    conditions.add(TABLE_LOG_ENTRY.Z.between(locZ ? loc1.z : loc2.z, locZ ? loc2.z : loc1.z));
                }
                else if (params.radius == null)// has single location
                {
                    conditions.add(TABLE_LOG_ENTRY.X.eq(loc1.x));
                    conditions.add(TABLE_LOG_ENTRY.Y.eq(loc1.y));
                    conditions.add(TABLE_LOG_ENTRY.Z.eq(loc1.z));
                }
                else // has radius
                {
                    conditions.add(TABLE_LOG_ENTRY.X.between(loc1.x-params.radius,loc1.x+params.radius));
                    conditions.add(TABLE_LOG_ENTRY.Y.between(loc1.y-params.radius,loc1.y+params.radius));
                    conditions.add(TABLE_LOG_ENTRY.Z.between(loc1.z-params.radius,loc1.z+params.radius));
                }
            }
        }
        if (!params.blocks.isEmpty())
        {
            // make sure there is data for blocks first
            conditions.add(TABLE_LOG_ENTRY.BLOCK.isNotNull().
                        or(TABLE_LOG_ENTRY.DATA.isNotNull()).
                        or(TABLE_LOG_ENTRY.NEWBLOCK.isNotNull()).
                        or(TABLE_LOG_ENTRY.NEWDATA.isNotNull()));
            // Start filter blocks:
            boolean include = params.includeBlocks();
            Condition blockCondition = null;
            for (Entry<ImmutableBlockData,Boolean> data : params.blocks.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    String mat = data.getKey().material.name();
                    Condition condition = TABLE_LOG_ENTRY.BLOCK.eq(mat).or(TABLE_LOG_ENTRY.NEWBLOCK.eq(mat));
                    Byte metadata = data.getKey().data;
                    if (metadata != null)
                    {
                        condition = condition.and(TABLE_LOG_ENTRY.DATA.eq(metadata.longValue()).or(TABLE_LOG_ENTRY.NEWDATA.eq(metadata)));
                    }
                    if (!include)
                    {
                        condition = condition.not();
                    }
                    if (blockCondition == null)
                    {
                        blockCondition = condition;
                    }
                    else
                    {
                        blockCondition = blockCondition.or(condition);
                    }
                }
            }
            if (blockCondition != null)
            {
                conditions.add(blockCondition);
            }
        }
        if (!params.users.isEmpty())
        {
            // Start filter users:
            boolean include = params.includeUsers();
            Collection<Long> users = new HashSet<>();
            for (Entry<Long,Boolean> data : params.users.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    users.add(data.getKey());
                }
            }
            if (include)
            {
                conditions.add(TABLE_LOG_ENTRY.CAUSER.in(users));
            }
            else
            {
                conditions.add(TABLE_LOG_ENTRY.CAUSER.notIn(users));
            }
        }
        // TODO finish queryParams

        SelectConditionStep<LogEntry> query = whereStep.where(conditions);
        this.module.getLog().debug("{}: Select Query queued!", user.getName());
        this.queuedLookups.offer(new QueuedSqlParams(lookup,user,query, action));
        if (this.futureLookup == null || this.futureLookup.isDone())
        {
            this.futureLookup = lookupExecutor.submit(lookupRunner);
        }
    }

    public Collection<ActionTypeModel> getActionTypesFromDatabase()
    {
        return this.dsl.selectFrom(TABLE_ACTION_TYPE).fetch();
    }

    public ActionTypeModel registerActionType(String name)
    {
        ActionTypeModel actionTypeModel = this.dsl.newRecord(TABLE_ACTION_TYPE).newActionType(name);
        actionTypeModel.insert();
        return actionTypeModel;
    }

    public void unregisterActionType(String name)
    {
        this.dsl.delete(TABLE_ACTION_TYPE).where(TABLE_ACTION_TYPE.NAME.eq(name)).execute();
    }

    public static class QueuedSqlParams
    {
        public final Lookup lookup;
        private final User user;
        public final QueryAction action;
        public final SelectConditionStep<LogEntry> query;

        public QueuedSqlParams(Lookup lookup, User user, SelectConditionStep<LogEntry> query, QueryAction action)
        {
            this.lookup = lookup;
            this.query = query;
            this.user = user;
            this.action = action;
        }
    }
}

