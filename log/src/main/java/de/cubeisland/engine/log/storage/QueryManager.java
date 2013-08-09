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
import java.sql.SQLException;
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

import de.cubeisland.engine.core.command.CommandSender;
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

    private CountDownLatch latch = null;

    private int cleanUpTaskId;

    private String mainTable = "log_entries";
    private String tempTable = "log_entries_temp";

    private Connection insertConnection = null;
    private DSLContext dsl;
    private DSLContext cleanUpDsl;
    private Database database;

    public QueryManager(Log module)
    {
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
        this.cleanUpLogs();
    }
    /*
        sql = builder.insert().into(table)
                     .cols("date", "action", "world", "x", "y", "z", "causer",
                           "block", "data", "newBlock", "newData", "additionalData")
                     .end().end();
        this.database.storeStatement(this.getClass(), "storeLog", sql);
    }
    */
    /**
     * ONLY execute async this could take a LONG time
     */
    private void optimizeTable(final CommandSender sender)
    {
        //if (true) return;// TODO do test if this locks the server
        try
        {
            this.database.execute("OPTIMIZE TABLE cube_"+ this.mainTable);
            sender.sendTranslated("&aOptimization finished! Copy temp-table...");
            this.database.startTransaction();
            this.database.execute("INSERT INTO cube_" + this.mainTable + " (date, action, world, x, y, z, causer, block, data, newBlock, newData, additionalData)"
                                      + " SELECT date, action, world, x, y, z, causer, block, data, newBlock, newData, additionalData FROM cube_" + this.tempTable);
            this.database.commit();
        }
        catch (SQLException e)
        {
            this.module.getLog().error("Error during optimization of log-tables", e);
        }
    }

    private boolean optimizeRunning = false;

    /**
     * Optimizes the indices of the log table
     *
     * @param sender
     */
    public void optimize(final CommandSender sender)
    {
        //TODO DATABASE
        /*
        if (true)
        {
            // TODO do this with a separate connection
            sender.sendMessage("NOT YET FINISHED");
            return;
        }
        if (optimizeRunning)
        {
            sender.sendTranslated("&cThe database is already busy optimizing.");
            return;
        }
        try
        {
            optimizeRunning = true;
            Profiler.startProfiling("log_optimize");
            sender.sendTranslated("&aStarted optimizing! This may take a while.");
            this.createTableWithInsertQuery(this.database.getQueryBuilder(), this.tempTable); // Create temp table & log into that table
            this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    optimizeTable(sender);
                    module.getCore().getTaskManager().runTask(module, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                createTableWithInsertQuery(database.getQueryBuilder(), mainTable);
                                database.execute(database.getQueryBuilder().dropTable(tempTable).end());
                                sender.sendTranslated("&adone in %d seconds! Indices are now optimized!", Profiler.endProfiling("log_optimize", TimeUnit.SECONDS));
                                optimizeRunning = false;
                            }
                            catch (SQLException e)
                            {
                                module.getLog().error("Error during optimization of log-tables", e);
                            }
                        }
                    });
                }
            }).start();
        }
        catch (SQLException e)
        {
            this.module.getLog().error("Error during optimization of log-tables", e);
        }*/
    }

    private void cleanUpLogs()
    {
        // if (true) return;// TODO do this with a separate db-connection
        Profiler.endProfiling("log_cleanUp");
        Profiler.startProfiling("log_cleanUp");
        if (this.module.getConfiguration().cleanUpDeletedWorlds)
        {
            long[] worlds = this.module.getCore().getWorldManager().getAllWorldIds();
            final UInteger[] values = new UInteger[worlds.length];
            for (int i = 0 ; i < worlds.length; i++)
            {
                values[i] = UInteger.valueOf(worlds[i]);
            }
            this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    cleanUpDsl.delete(TABLE_LOG_ENTRY).where(TABLE_LOG_ENTRY.WORLD.notIn(values)).execute();
                }
            }).start();
        }
        if (this.module.getConfiguration().cleanUpOldLogs)
        {
            this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    cleanUpDsl.delete(TABLE_LOG_ENTRY).where(
                        TABLE_LOG_ENTRY.DATE.le(new Timestamp(System.currentTimeMillis() - module.getConfiguration().cleanUpOldLogsTime.toMillis()))).execute();
                }
            }).start();
        }
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
        Result<LogEntry> entries = poll.query.fetch();
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
                .insertInto(TABLE_LOG_ENTRY, TABLE_LOG_ENTRY.DATE, TABLE_LOG_ENTRY.ACTION, TABLE_LOG_ENTRY.WORLD, TABLE_LOG_ENTRY.X, TABLE_LOG_ENTRY.Y, TABLE_LOG_ENTRY.Z, TABLE_LOG_ENTRY.CAUSER, TABLE_LOG_ENTRY.BLOCK, TABLE_LOG_ENTRY.DATA, TABLE_LOG_ENTRY.NEWBLOCK, TABLE_LOG_ENTRY.NEWDATA, TABLE_LOG_ENTRY.ADDITIONALDATA)
                .values((Timestamp)null, null, null, null, null, null, null, null, null, null, null, null).getSQL();
            if (this.insertConnection == null)
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
                this.module.getLog().debug("{} logged in: {} ms | remaining logs: {}",
                                         logSize, TimeUnit.NANOSECONDS.toMillis(nanos), queuedLogs.size());
                this.module.getLog().debug("Average logtime per log: {} micros",
                                           TimeUnit.NANOSECONDS.toMicros(timeSpend / logsLogged));
                if (timeSpendFullLoad != 0)
                {
                    this.module.getLog().debug("Average logtime per log in full load: {} micros",
                                               TimeUnit.NANOSECONDS.toMicros(timeSpendFullLoad / logsLoggedFullLoad));
                }
            }
            if (!queuedLogs.isEmpty())
            {
                this.futureStore = this.storeExecutor.submit(this.storeRunner);
            }
            else if (this.latch != null)
            {
                this.insertConnection.close();
                this.insertConnection = null;
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
        this.module.getCore().getTaskManager().cancelTask(this.module, cleanUpTaskId);
        if (!this.queuedLogs.isEmpty())
        {
            this.batchSize = this.batchSize * 5;
            this.lookupExecutor.shutdown();
            latch = new CountDownLatch(1);
            try {
                latch.await(3, TimeUnit.MINUTES); // wait for all logs to be logged (stop after 3 min)
            } catch (InterruptedException e) {
                this.module.getLog().warn("Error while waiting! " + e.getLocalizedMessage(), e);
            }
            this.storeExecutor.shutdown();
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

