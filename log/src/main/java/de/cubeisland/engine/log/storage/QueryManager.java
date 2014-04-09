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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.ActionBlock.BlockSection;

public class QueryManager
{
    final Queue<BaseAction> queuedLogs = new ConcurrentLinkedQueue<>();
    final Queue<QueuedSqlParams> queuedLookups = new ConcurrentLinkedQueue<>();
    private final Log module;
    private final ExecutorService storeExecutor;
    private final Runnable storeRunner;
    private final ExecutorService lookupExecutor;
    private final Runnable lookupRunner;
    private final Semaphore latch = new Semaphore(1);
    private final int cleanUpTaskId;
    private int batchSize;
    private Future<?> futureStore = null;
    private Future<?> futureLookup = null;
    private long timeSpend = 0;
    private long logsLogged = 1;
    private long timeSpendFullLoad = 0;
    private long logsLoggedFullLoad = 1;
    private CountDownLatch shutDownLatch = new CountDownLatch(0);
    private Connection insertConnection = null;
    private boolean cleanUpRunning = false;

    private final DBCollection collection;

    public QueryManager(Log module, DBCollection collection)
    {
        this.collection = collection;
        this.module = module;

        this.batchSize = module.getConfiguration().loggingBatchSize;

        this.storeRunner = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    doEmptyLogs(batchSize);
                }
                catch (Exception ex)
                {
                    QueryManager.this.module.getLog().error(ex, "Fatal Error while logging!");
                }
            }
        };

        final ThreadFactory factory = this.module.getCore().getTaskManager().getThreadFactory(this.module);
        this.storeExecutor = Executors.newSingleThreadExecutor(factory);
        this.lookupRunner = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    doQueryLookup();
                }
                catch (Exception ex)
                {
                    QueryManager.this.module.getLog().error(ex, "Error while lookup!");
                }
            }
        };
        this.lookupExecutor = Executors.newSingleThreadExecutor(factory);

        long delay = this.module.getConfiguration().cleanup.delay.getStandardSeconds() * 20;
        this.cleanUpTaskId = this.module.getCore().getTaskManager().runAsynchronousTimer(this.module, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    cleanUpLogs();
                }
                catch (Exception e)
                {
                    QueryManager.this.module.getLog().error(e, "An error occurred while cleaning up the database");
                }
            }
        }, delay, delay);
    }

    private void cleanUpLogs() throws InterruptedException
    {
        if (CubeEngine.isMainThread())
        {
            throw new IllegalStateException("ONLY use Asynchronously!");
        }
        if (cleanUpRunning)
        {
            this.module.getLog().warn("CleanUp - Is currently running cannot start again!");
            return;
        }
        int steps = this.module.getConfiguration().cleanup.steps;
        this.module.getLog().debug("!! Started automatic log_entries table cleanup !!");
        this.module.getLog().debug("(The CleanUp can take a VERY long time)");
        this.cleanUpRunning = true;
        if (this.module.getConfiguration().cleanup.oldLogs.enable)
        {
            BasicDBObject condition = new BasicDBObject("date", new BasicDBObject("$lt", new Date(System.currentTimeMillis() - module.getConfiguration().cleanup.oldLogs.time.getMillis())));
            BasicDBObject fields = new BasicDBObject("_id", 1);
            this.latch.acquire();

            DBCursor toRemove = this.collection.find(condition, fields);
            int count = toRemove.count();
            if (count != 0)
            {
                toRemove.skip(count < steps ? count - 1 : steps - 1).limit(1);
                this.module.getLog().debug("CleanUp - CleanUp of {} old logs", toRemove.count());
                int i = 0;
                while (count > 0)
                {
                    i += this.collection.remove(new BasicDBObject("_id", new BasicDBObject("$lte", toRemove.next().get("_id")))).getN();
                    this.latch.release();
                    this.module.getLog().debug("CleanUp - {} logs deleted", i);
                    this.latch.acquire();
                    toRemove = this.collection.find(condition, fields);
                    count = toRemove.count();
                    toRemove.skip(count < steps ? count - 1 : steps - 1).limit(1);
                }
            }
            else
            {
                this.module.getLog().debug("CleanUp - CleanUp of old logs; Skipped");
            }
            this.latch.release();
        }
        else
        {
            this.module.getLog().debug("CleanUp - CleanUp of old logs; Skipped");
        }
        if (this.module.getConfiguration().cleanup.deletedWorlds)
        {
            this.module.getConfiguration().cleanup.deletedWorlds = false;
            this.module.getConfiguration().save();
            List<UUID> uuids = new ArrayList<>(this.module.getCore().getWorldManager().getAllWorldUUIDs());
            BasicDBObject condition = new BasicDBObject("coord.world-uuid", new BasicDBObject("$uuid", uuids));
            BasicDBObject fields = new BasicDBObject("_id", 1);
            this.latch.acquire();
            DBCursor toRemove = this.collection.find(condition, fields);
            int count = toRemove.count();
            if (count != 0)
            {
                toRemove.skip(count < steps ? count - 1 : steps - 1).limit(1);
                this.module.getLog().debug("CleanUp - CleanUp of {} logs in deleted worlds", toRemove.count());
                int i = 0;
                while (count > 0)
                {
                    i += this.collection.remove(new BasicDBObject("_id", new BasicDBObject("$lte", toRemove.next().get("_id")))).getN();
                    this.latch.release();
                    this.module.getLog().debug("CleanUp - {} logs deleted", i);
                    this.latch.acquire();
                    toRemove = this.collection.find(condition, fields);
                    count = toRemove.count();
                    toRemove.skip(count < steps ? count - 1 : steps - 1).limit(1);
                }
            }
            else
            {
                this.module.getLog().debug("CleanUp - CleanUp of deleted worlds; Skipped");
            }
            this.latch.release();
        }
        else
        {
            this.module.getLog().debug("CleanUp - CleanUp of deleted worlds; Skipped");
        }
        this.cleanUpRunning = false;
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
        DBCursor cursor = this.collection.find(poll.query); // limit 10000
        QueryResults results = new QueryResults(lookup, module);
        for (DBObject entry : cursor)
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<? extends BaseAction> action = (Class<? extends BaseAction>)Class.forName((String)entry.get("action"));
                results.addResult(module.getCore().getConfigFactory().load(action, entry));
            }
            catch (ClassNotFoundException e)
            {
                module.getLog().warn(e, "Could not find Action for DBObject! {}", entry.get("action"));
            }
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
                        lookup.redo(user, false);
                        return;
                    case REDO_PREVIEW:
                        lookup.redo(user, true);
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
        final Queue<BaseAction> logs = new LinkedList<>();
        try
        {
            this.latch.acquire();  // Wait if still doing inserts
            if (queuedLogs.isEmpty())
            {
                return;
            }
            for (int i = 0; i < amount; i++) // log <amount> next logs...
            {
                BaseAction toLog = this.queuedLogs.poll();
                if (toLog == null)
                {
                    break;
                }
                // TODO if toLog has reference DO NOT log in batch!!! or does it work?
                logs.offer(toLog);
            }

            Profiler.startProfiling("logging");
            int logSize = logs.size();
            List<DBObject> toLog = new ArrayList<>();
            for (BaseAction log : logs)
            {
                log.save();
                toLog.add(log.getTarget());
                log.getTarget().put("action", log.getClass().getName());
            }
            this.collection.insert(toLog); // Batch insert

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
                this.module.getLog()
                           .debug("{} logged in: {} ms | remaining logs: {} | AVG/AVG-FULL {} / {} micros", logSize, TimeUnit.NANOSECONDS
                               .toMillis(nanos), queuedLogs.size(), TimeUnit.NANOSECONDS
                                      .toMicros(timeSpend / logsLogged), TimeUnit.NANOSECONDS
                                      .toMicros(timeSpendFullLoad / logsLoggedFullLoad));
            }
        }
        catch (Exception ex)
        {
            module.getLog().error(ex, "Error while logging!");
            this.queuedLogs.addAll(logs);
            if (latch.availablePermits() == 0)
            {
                this.latch.release();
            }
            Profiler.endProfiling("logging"); // end profiling so we can start again later
            try
            {
                insertConnection.close();
            }
            catch (SQLException e)
            {
                module.getLog().error(e, "Error when closing connection!");
            }
            finally
            {
                insertConnection = null;
            }
        }
        finally
        {
            if (latch.availablePermits() == 0)
            {
                latch.release();
            }
            if (!queuedLogs.isEmpty())
            {
                this.futureStore = this.storeExecutor.submit(this.storeRunner);
            }
            else
            {
                if (insertConnection != null)
                {
                    try
                    {
                        this.insertConnection.setAutoCommit(true);
                        this.insertConnection.close();
                    }
                    catch (SQLException e)
                    {
                        module.getLog().error(e, "Error when closing connection!");
                    }
                    this.insertConnection = null;
                }
                if (shutDownLatch != null)
                {
                    this.shutDownLatch.countDown();
                }
            }
        }
    }

    private void release()
    {
        if (this.latch.availablePermits() == 1)
        {
            this.latch.release();
        }
    }

    protected void queueLog(BaseAction action)
    {
        this.queuedLogs.offer(action);
        if (this.latch.availablePermits() == 1 && (this.futureStore == null || this.futureStore.isDone()))
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
        try
        {
            shutDownLatch.await(1, TimeUnit.MINUTES); // wait for all logs to be logged (stop after 3 min)
        }
        catch (InterruptedException ex)
        {
            this.module.getLog().warn(ex, "Error while waiting! ");
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
                this.module.getLog().warn(new Exception(), "Logging doesn't seem to progress! Aborting...");
            }
        }
    }

    public void logStatus()
    {
        this.module.getLog().info("{} logs queued!", this.queuedLogs.size());
        this.module.getLog().info("Permit availiable? {}", this.latch.availablePermits() == 1);

        if (this.futureStore != null)
        {
            this.module.getLog().info("FutureStore is {}done!", this.futureStore.isDone() ? "" : "NOT ");
        }
    }

    public void prepareLookupQuery(final Lookup lookup, final User user, QueryAction action)
    {
        if (this.cleanUpRunning)
        {
            switch (action)
            {
            case SHOW:
                user.sendTranslated(MessageType.NEUTRAL, "Lookups cannot return all data while cleaning up the database!");
                break;
            case ROLLBACK:
            case REDO:
            case ROLLBACK_PREVIEW:
            case REDO_PREVIEW:
                user.sendTranslated(MessageType.NEGATIVE, "This action is not possible while cleaning up the database!");
                user.sendTranslated(MessageType.NEUTRAL, "Please wait");
                return;
            }
        }
        final QueryParameter params = lookup.getQueryParameter();
        BasicDBObject query = new BasicDBObject();

        if (params.world != null) // has world
        {
            query.append("coord.world-uuid", params.world.getUID().toString());
            if (params.location1 != null)
            {
                BlockVector3 loc1 = params.location1;
                if (params.location2 != null)// has area
                {
                    BlockVector3 loc2 = params.location2;
                    boolean locX = loc1.x < loc2.x;
                    boolean locY = loc1.y < loc2.y;
                    boolean locZ = loc1.z < loc2.z;
                    
                    query.append("coord.vector.x", new BasicDBObject("$gte", locX ? loc1.x : loc2.x).append("$lte", locX ? loc2.x : loc1.x)).
                          append("coord.vector.y", new BasicDBObject("$gte", locY ? loc1.y : loc2.y).append("$lte", locX ? loc2.y : loc1.y)).
                          append("coord.vector.z", new BasicDBObject("$gte", locZ ? loc1.z : loc2.z).append("$lte", locX ? loc2.z : loc1.z));
                }
                else if (params.radius == null)// has single location
                {
                    query.append("coord.vector.x", loc1.x).
                          append("coord.vector.y", loc1.y).
                          append("coord.vector.z", loc1.z);
                }
                else // has radius
                {
                    query.append("coord.vector.x", new BasicDBObject("$gte", loc1.x - params.radius).append("$lte", loc1.x + params.radius)).
                          append("coord.vector.y", new BasicDBObject("$gte", loc1.y - params.radius).append("$lte", loc1.y + params.radius)).
                          append("coord.vector.z", new BasicDBObject("$gte", loc1.z - params.radius).append("$lte", loc1.z + params.radius));
                }
            }
        }
        if (!params.actions.isEmpty())
        {
            boolean include = params.includeActions();
            Collection<String> actions = new HashSet<>();
            for (Entry<Class<? extends BaseAction>, Boolean> entry : params.actions.entrySet())
            {
                if (!include || entry.getValue())
                {
                    actions.add(entry.getKey().getName());
                }
            }
            if (!include)
            {
                query.append("action", new BasicDBObject("$nin", actions));
            }
            else
            {
                query.append("action", new BasicDBObject("$in", actions));
            }
        }
        if (params.hasTime()) // has since / before / from-to
        {
            if (params.from_since == null) // before
            {
                query.append("date", new BasicDBObject("$lte", params.to_before));
            }
            else if (params.to_before == null) // since
            {
                query.append("date", new BasicDBObject("$gte", params.from_since));
            }
            else // from - to
            {
                query.append("date", new BasicDBObject("$gte", params.from_since).append("$lte", params.to_before));
            }
        }

        if (!params.blocks.isEmpty())
        {
            // Start filter blocks:
            boolean include = params.includeBlocks();
            List<DBObject> list = new ArrayList<>();
            List<String> mList = new ArrayList<>();

            for (Entry<BlockSection, Boolean> data : params.blocks.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    BlockSection block = data.getKey();
                    if (block.data == null)
                    {
                        mList.add(block.material.name());
                    }
                    else
                    {
                        list.add(new BasicDBObject("material", block.material.name()).append("data", block.data));
                    }
                }
            }
            List<BasicDBObject> blockQuery = new ArrayList<>();
            if (!list.isEmpty())
            {
                BasicDBObject dboL = new BasicDBObject("$in", list);
                blockQuery.add(new BasicDBObject("old-block", dboL));
                blockQuery.add(new BasicDBObject("new-block", dboL));
            }
            if (!mList.isEmpty())
            {
                BasicDBObject dboML = new BasicDBObject("$in", mList);
                blockQuery.add(new BasicDBObject("old-block.material", dboML));
                blockQuery.add(new BasicDBObject("new-block.material", dboML));
            }
            if (include)
            {
                query.append("$or", blockQuery);
            }
            else
            {
                query.append("$not", new BasicDBObject("$or", blockQuery));
            }
        }
        if (!params.users.isEmpty())
        {
            // Start filter users:
            boolean include = params.includeUsers();
            List<String> players = new ArrayList<>();
            for (Entry<UUID, Boolean> data : params.users.entrySet())
            {
                if (!include || data.getValue()) // all exclude OR only include
                {
                    players.add(data.getKey().toString());
                }
            }
            if (include)
            {
                query.append("player.uuid", new BasicDBObject("$in", players));
            }
            else
            {
                query.append("player.uuid", new BasicDBObject("$nin", players));
            }
        }
        // TODO finish queryParams

        this.module.getLog().debug("{}: Select Query queued!", user.getName());
        this.queuedLookups.offer(new QueuedSqlParams(lookup, user, query, action));
        if (this.futureLookup == null || this.futureLookup.isDone())
        {
            this.futureLookup = lookupExecutor.submit(lookupRunner);
        }
    }

    public enum QueryAction
    {
        SHOW,
        ROLLBACK,
        REDO,
        ROLLBACK_PREVIEW,
        REDO_PREVIEW;
    }

    public static class QueuedSqlParams
    {
        public final Lookup lookup;
        public final QueryAction action;
        public final BasicDBObject query;
        private final User user;

        public QueuedSqlParams(Lookup lookup, User user, BasicDBObject query, QueryAction action)
        {
            this.lookup = lookup;
            this.query = query;
            this.user = user;
            this.action = action;
        }
    }

    private class MutableInteger
    {
        public int value;

        private MutableInteger(int value)
        {
            this.value = value;
        }
    }
}

