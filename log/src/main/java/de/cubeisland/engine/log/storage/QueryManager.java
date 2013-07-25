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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.ActionType;
import gnu.trove.map.hash.THashMap;

import static de.cubeisland.engine.core.storage.database.querybuilder.ComponentBuilder.IS;

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
            this.mainTable = "log_entries";
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
            this.createTableWithInsertQuery(builder, this.mainTable);
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

    private void createTableWithInsertQuery(QueryBuilder builder, String table) throws SQLException
    {
        String sql = builder.createTable(table, true).beginFields()
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
        sql = builder.insert().into(table)
                     .cols("date", "action", "world", "x", "y", "z", "causer",
                           "block", "data", "newBlock", "newData", "additionalData")
                     .end().end();
        this.database.storeStatement(this.getClass(), "storeLog", sql);
    }

    /**
     * ONLY execute async this could take a LONG time
     */
    private void optimizeTable(final CommandSender sender)
    {
        if (true) return;// TODO do this with a separate db-connection
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
        }
    }

    private void cleanUpLogs()
    {
        if (true) return;// TODO do this with a separate db-connection
        Profiler.endProfiling("log_cleanUp");
        Profiler.startProfiling("log_cleanUp");
        if (this.module.getConfiguration().cleanUpDeletedWorlds)
        {
            long[] worlds = this.module.getCore().getWorldManager().getAllWorldIds();
            Long[] values = new Long[worlds.length];
            for (int i = 0 ; i < worlds.length; i++)
            {
                values[i] = worlds[i];
            }
            final String query = this.database.getQueryBuilder().deleteFrom("log_entries").where().field("world").not().in()
                                      .valuesInBrackets(values).end().end();
            this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        database.execute(query);
                        module.getLog().debug("Deleted logs from deleted worlds! {} ms", Profiler.getCurrentDelta("log_cleanUp", TimeUnit.MILLISECONDS));
                    }
                    catch (SQLException e)
                    {
                        module.getLog().error("Could not clean up deleted world logs!", e);
                    }
                }
            }).start();
        }
        if (this.module.getConfiguration().cleanUpOldLogs)
        {
            final String query = this.database.getQueryBuilder().deleteFrom("log_entries")
                              .where().field("date").is(ComponentBuilder.LESS)
                              .value(new Timestamp(System.currentTimeMillis() - this.module.getConfiguration()
                                  .cleanUpOldLogsTime.toMillis())).end().end();
            this.module.getCore().getTaskManager().getThreadFactory().newThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        database.execute(query);
                        module.getLog().debug("Deleted old logs! {} ms",Profiler.getCurrentDelta("log_cleanUp", TimeUnit.MILLISECONDS));
                    }
                    catch (SQLException e)
                    {
                        module.getLog().error("Could not clean up old logs!", e);
                    }
                }
            }).start();
        }
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
            final QueryAction queryAction = poll.action;
            final Lookup lookup = poll.lookup;
            final User user = poll.user;
            PreparedStatement stmt = this.database.prepareStatement(poll.sql);
            for (int i = 0 ; i <  poll.sqlData.size() ; ++i)
            {
                stmt.setObject(i+1, poll.sqlData.get(i));
            }
            ResultSet resultSet = stmt.executeQuery();
            QueryResults results = new QueryResults(lookup);
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
            boolean include = params.includeActions();
            if (!include)
            {
                selectBuilder.not();
            }
            selectBuilder.in().valuesInBrackets(params.actions.size()).endSub();
            for (Entry<ActionType,Boolean> type : params.actions.entrySet())
            {
                if (!include || type.getValue()) // all exclude OR only include
                {
                    dataToInsert.add(type.getKey().getID());
                }
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
                    boolean locX = loc1.x < loc2.x;
                    boolean locY = loc1.y < loc2.y;
                    boolean locZ = loc1.z < loc2.z;
                    selectBuilder.and().beginSub()
                        .field("x").between(locX ? loc1.x : loc2.x, locX ? loc2.x : loc1.x)
                        .and().field("y").between(locY ? loc1.y : loc2.y, locY ? loc2.y : loc1.y)
                        .and().field("z").between(locZ ? loc1.z : loc2.z, locZ ? loc2.z : loc1.z)
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
                else // has radius
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
        if (!params.blocks.isEmpty())
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            selectBuilder.beginSub();
            // make sure there is data for blocks first
            selectBuilder.not().beginSub().field("block").is(IS).value(null).or()
                         .field("data").is(IS).value(null).or()
                         .field("newBlock").is(IS).value(null).or()
                         .field("newData").is(IS).value(null).endSub();
            // Start filter blocks:
            selectBuilder.and();
            boolean include = params.includeBlocks();
            if (!include)
            {
                selectBuilder.not();
            }
            selectBuilder.beginSub();
            boolean or = false;
            for (Entry<ImmutableBlockData,Boolean> data : params.blocks.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    if (or)
                    {
                        selectBuilder.or();
                    }
                    selectBuilder.beginSub();
                    selectBuilder.field("block").isEqual().value(data.getKey().material.name()).or().field("newBlock").isEqual().value(data.getKey().material.name());
                    if (data.getKey().data != null)
                    {
                        selectBuilder.and().beginSub().field("data").isEqual().value(data.getKey().data).or().field("newData").isEqual().value(data.getKey().data).endSub();
                    }
                    selectBuilder.endSub();
                    or = true;
                }
            }
            selectBuilder.endSub().endSub();
            needAnd = true;
        }
        if (!params.users.isEmpty())
        {
            if (needAnd)
            {
                selectBuilder.and();
            }
            // Start filter users:
            boolean include = params.includeUsers();
            if (!include)
            {
                selectBuilder.not();
            }
            selectBuilder.beginSub();
            boolean or = false;
            for (Entry<Long,Boolean> data : params.users.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    if (or)
                    {
                        selectBuilder.or();
                    }
                    selectBuilder.field("causer").isEqual().value(data.getKey());
                    or = true;
                }
            }
            selectBuilder.endSub();
            needAnd = true;
        }
        // TODO finish queryParams
        String sql = selectBuilder.end().end();
        this.module.getLog().debug("{}: Lookup queued!", user.getName());
        this.queuedLookups.offer(new QueuedSqlParams(lookup,user,sql,dataToInsert, action));
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
        public final QueryAction action;

        public QueuedSqlParams(Lookup lookup, User user, String sql, ArrayList sqlData, QueryAction action)
        {
            this.lookup = lookup;
            this.sql = sql;
            this.sqlData = sqlData;
            this.user = user;
            this.action = action;
        }
    }
}
