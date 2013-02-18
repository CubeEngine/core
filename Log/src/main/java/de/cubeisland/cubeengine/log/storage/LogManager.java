package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.util.worker.AsyncTaskQueue;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.BlockLogger;
import de.cubeisland.cubeengine.log.lookup.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogManager
{
    public static final int BLOCK_PLACE = 0x00;
    public static final int BLOCK_BREAK = 0x01;
    public static final int BLOCK_CHANGE = 0x02; //changing blockdata / interactlog
    public static final int BLOCK_SIGN = 0x03; //sign change
    public static final int BLOCK_GROW_BP = 0x04; //growth induced by a player
    public static final int BLOCK_CHANGE_WE = 0x05;
    public static final int BLOCK_EXPLODE = 0x06; // Creeper attacking a player
    public static final int KILL_PVP = 0x10; //player killed by player
    public static final int KILL_PVE = 0x11; //player killed by environement
    public static final int KILL_EVE = 0x12; //mob killed by environement
    public static final int KILL_EVP = 0x13; //mob killed by player
    public static final int CHAT = 0x20;
    public static final int COMMAND = 0x21;
    public static final int CHEST_PUT = 0x30;
    public static final int CHEST_TAKE = 0x31;
    private final AsyncTaskQueue taskQueue = new AsyncTaskQueue(CubeEngine.getTaskManager().getExecutorService());
    private final Database database;
    private final Log module;
    private final PreparedStatement storeLog;
    private int logBuffer = 2000; // TODO config
    private final int repeatingTaskId;

    public LogManager(Log module)
    {
        this.database = module.getDatabase();
        this.module = module;
        try
        {
            // Main table:
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("log_logs", true).beginFields()
                    .field("key", AttrType.INT, true).autoIncrement()
                    .field("date", AttrType.TIMESTAMP)
                    .field("world_id", AttrType.INT, true, false)
                    .field("x", AttrType.INT, false, false)
                    .field("y", AttrType.INT, false, false)
                    .field("z", AttrType.INT, false, false)
                    .field("action", AttrType.TINYINT)
                    .field("causer", AttrType.BIGINT)
                    .foreignKey("world_id").references("worlds", "key")
                    .index("x")
                    .index("y")
                    .index("z")
                    .index("action")
                    .index("causer")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_logs")
                    .cols("date", "world_id", "x", "y", "z", "action", "causer")
                    .end().end();
            this.storeLog = this.database.prepareStatement(sql);
            //Block logging:
            sql = builder.createTable("log_block", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldBlock", AttrType.INT, false, false)
                    .field("oldBlockData", AttrType.TINYINT, false, false)
                    .field("newBlock", AttrType.INT, false, false)
                    .field("newBlockData", AttrType.TINYINT, false, false)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_block")
                    .cols("key", "oldBlock", "oldBlockData", "newBlock", "newBlockData")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeBlockLog", sql);
            //Sign logging:
            sql = builder.createTable("log_sign", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("oldLine1", AttrType.VARCHAR, 16, false)
                    .field("oldLine2", AttrType.VARCHAR, 16, false)
                    .field("oldLine3", AttrType.VARCHAR, 16, false)
                    .field("oldLine4", AttrType.VARCHAR, 16, false)
                    .field("newLine1", AttrType.VARCHAR, 16, false)
                    .field("newLine2", AttrType.VARCHAR, 16, false)
                    .field("newLine3", AttrType.VARCHAR, 16, false)
                    .field("newLine4", AttrType.VARCHAR, 16, false)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_sign")
                    .cols("key", "oldLine1", "oldLine2", "oldLine3",
                        "oldLine4", "newLine1", "newLine2", "newLine3", "newLine4")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeSignLog", sql);
            //Kill logging:
            sql = builder.createTable("log_kill", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("killed", AttrType.BIGINT)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_kill")
                    .cols("key", "killed")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeKillLog", sql);
            //Message (Chat/Command) logging:
            sql = builder.createTable("log_message", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("message", AttrType.VARCHAR, 100)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_message")
                    .cols("key", "message")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeMessageLogs", sql);
            //Chest logging:
            sql = builder.createTable("log_chest", true).beginFields()
                    .field("key", AttrType.INT, true)
                    .field("item", AttrType.INT)
                    .field("data", AttrType.INT)
                    .field("name", AttrType.VARCHAR, 50)
                    .field("amount", AttrType.INT)
                    .field("containerType", AttrType.INT)
                    .foreignKey("key").references("log_logs", "key").onDelete("CASCADE")
                    .primaryKey("key").endFields()
                    .engine("innoDB").defaultcharset("utf8")
                    .end().end();
            this.database.execute(sql);
            sql = builder.insert().into("log_chest")
                    .cols("key", "item", "data", "name", "amount", "containerType")
                    .end().end();
            this.database.storeStatement(this.getClass(), "storeChestLogs", sql);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error during initialization of log-tables", ex);
        }
        this.repeatingTaskId = CubeEngine.getTaskManager().scheduleSyncRepeatingTask(module, new Runnable()
        {
            @Override
            public void run()
            {
                taskQueue.addTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doEmptyLogs(logBuffer);
                    }
                });
            }
        }, 20, 20);
    }

    private Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<QueuedLog>();

    /**
     * Main log entry
     *
     * @param world
     * @param location
     * @param action
     * @param causer
     * @return
     */
    private void storeLog(World world, Location location, int action, long causer, Timestamp current, QueuedLog log)
    {
        Long world_id = world == null ? null : this.module.getCore().getWorldManager().getWorldId(world);
        Integer x = location == null ? null : location.getBlockX();
        Integer y = location == null ? null : location.getBlockY();
        Integer z = location == null ? null : location.getBlockZ();
        log.addMainLogData(current, world_id, x, y, z, action, causer);

        this.queuedLogs.offer(log);
    }

    private volatile boolean running = false;

    private void doEmptyLogs(int amount)
    {
        if (queuedLogs.isEmpty())
        {
            return;
        }
        if (running)
        {
            System.out.print("Already running!" + queuedLogs.size());
            return;
        }
        running = true;
        final Queue<QueuedLog> logs = new LinkedList<QueuedLog>();
        for (int i = 0; i < amount; i++) // log <smount> next logs...
        {
            QueuedLog toLog = this.queuedLogs.poll();
            if (toLog == null)
            {
                break;
            }
            logs.offer(toLog);
        }
        long a = System.currentTimeMillis();
        int logSize = logs.size();
        try
        {
            for (QueuedLog log : logs)
            {
                log.addMainDataToBatch(this.storeLog);
            }
            this.database.getConnection().setAutoCommit(false);
            this.storeLog.executeBatch();
            ResultSet genKeys = storeLog.getGeneratedKeys();
            while (genKeys.next())
            {
                long key = genKeys.getLong("GENERATED_KEY");
                logs.poll().run(key);
            }
            this.database.getConnection().commit();
            this.database.getConnection().setAutoCommit(true);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing main Log-Entry", ex, storeLog);
        }
        finally
        {
            running = false;
        }
        a = System.currentTimeMillis() - a;
        if (logSize == logBuffer)
            System.out.println("Logged " + logSize + " logs in: " + a / 1000 + "." + a % 1000 + "s | remaining logs: " + queuedLogs.size());
    }

    /**
     * Block log entry
     *
     * @param logID
     * @param oldData
     * @param newData
     */
    private void storeBlockLog(long logID, BlockData oldData, BlockData newData)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeBlockLog", logID,
                    oldData == null ? null : oldData.mat.getId(), oldData == null ? null : oldData.data,
                    newData == null ? null : newData.mat.getId(), newData == null ? null : newData.data);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing block Log-Entry", ex, this.database.getStoredStatement(this.getClass(),"storeBlockLog"));
        }
    }

    /**
     * Sign log entry
     *
     * @param logID
     * @param oldLines
     * @param newLines
     */
    private void storeSignLog(long logID, String[] oldLines, String[] newLines)
    {
        try
        {
            if (oldLines == null)
            {
                this.database.preparedExecute(this.getClass(), "storeSignLog", logID,
                        null, null, null, null,
                        newLines[0], newLines[1], newLines[2], newLines[3]);
            }
            else if (newLines == null)
            {
                this.database.preparedExecute(this.getClass(), "storeSignLog", logID,
                        oldLines[0], oldLines[1], oldLines[2], oldLines[3],
                        null, null, null, null);
            }
            else
            {
                this.database.preparedExecute(this.getClass(), "storeSignLog", logID,
                        oldLines[0], oldLines[1], oldLines[2], oldLines[3],
                        newLines[0], newLines[1], newLines[2], newLines[3]);
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing sign Log-Entry", ex, this.database.getStoredStatement(this.getClass(),"storeSignLog"));
        }
    }

    /**
     * Killed log entry
     *
     * @param logID
     * @param killed
     */
    private void storeKillLog(long logID, long killed)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeKillLog", logID, killed);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing kill Log-Entry", ex, this.database.getStoredStatement(this.getClass(),"storeKillLog"));
        }
    }

    /**
     * Chat or command entry
     *
     * @param logID
     * @param message
     */
    private void storeMessageLog(long logID, String message)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "storeMessageLogs", logID, message);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing message Log-Entry", ex, this.database.getStoredStatement(this.getClass(),"storeMessageLogs"));
        }
    }

    /**
     * Chest entry
     *
     * @param logID
     * @param itemData
     * @param amount
     * @param containerType
     */
    private void storeChestLog(long logID, ItemData itemData, int amount, int containerType)
    {
        try
        {

            this.database.preparedExecute(this.getClass(), "storeChestLogs",
                    logID, itemData.mat, itemData.data, itemData.name,
                    amount, containerType);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing message Log-Entry", ex, this.database.getStoredStatement(this.getClass(),"storeChestLogs"));
        }
    }

    /**
     * Blocks placed or destroyed
     *
     * @param cause
     * @param causerId
     * @param newState
     * @param oldState
     */
    public void logBlockLog(BlockLogger.BlockChangeCause cause, long causerId, World world, BlockState newState, BlockState oldState)
    {
        Location location;
        int type;
        if (newState != null)
        {
            type = BLOCK_PLACE;
            location = newState.getLocation();
        }
        else if (oldState != null)
        {
            type = BLOCK_BREAK;
            location = oldState.getLocation();
        }
        else
        {
            throw new IllegalArgumentException("Both states cannot be null!");
        }
        if (cause.equals(BlockLogger.BlockChangeCause.WORLDEDIT))
        {
            type = BLOCK_CHANGE_WE;
        }
        else if (cause.equals(BlockLogger.BlockChangeCause.GROW) && causerId > 0)
        {
            type = BLOCK_GROW_BP;
        }
        else if (cause.equals(BlockLogger.BlockChangeCause.EXPLOSION) && causerId > 0)
        {
            type = BLOCK_EXPLODE;
        }
        this.logBlockLog(location, type, causerId, world, BlockData.get(oldState), BlockData.get(newState));
    }

    /**
     * Blocks changed (interaction)
     *
     * @param causerId
     * @param world
     * @param state
     * @param newData
     */
    public void logBlockChange(long causerId, World world, BlockState state, byte newData)
    {
        Location location = state.getLocation();
        this.logBlockLog(location, BLOCK_CHANGE, causerId, world, BlockData.get(state), BlockData.get(state, newData));
    }

    private void logBlockLog(final Location location, final int type, final long causerId, final World world, final BlockData oldData, final BlockData newData)
    {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.storeLog(world, location, type, causerId, timestamp, new QueuedLog()
        {
            @Override
            public void run()
            {
                storeBlockLog(this.getInsertId(), oldData, newData);
            }
        });
    }

    /**
     * Sign changed
     *
     * @param causerId
     * @param location
     * @param oldlines
     * @param newlines
     */
    public void logSignLog(final long causerId, final Location location, final String[] oldlines, final String[] newlines)
    {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.storeLog(location.getWorld(), location, BLOCK_SIGN, causerId, timestamp, new QueuedLog()
        {
            @Override
            public void run()
            {
                storeSignLog(this.getInsertId(), oldlines, newlines);
            }
        });
    }

    /**
     * Entity killed
     *
     * @param killer
     * @param location
     * @param killed
     */
    public void logKillLog(final long killer, final Location location, final long killed)
    {
        final int type;
        if (killer > 0)
        {
            if (killed > 0)
            {
                type = KILL_PVP;
            }
            else
            {
                type = KILL_PVE;
            }
        }
        else
        {
            if (killed > 0)
            {
                type = KILL_EVP;
            }
            else
            {
                type = KILL_EVE;
            }
        }
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        this.storeLog(location.getWorld(), location, type, killer, timestamp, new QueuedLog()
        {
            @Override
            public void run()
            {
                storeKillLog(this.getInsertId(), killed);
            }
        });
    }

    /**
     * Message typed
     *
     * @param causerId
     * @param world
     * @param location
     * @param message
     * @param isChat
     */
    public void logChatLog(final long causerId, final World world, final Location location, final String message, final boolean isChat)
    {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.storeLog(world, location, isChat ? CHAT : COMMAND, causerId, timestamp, new QueuedLog() {
            @Override
            public void run()
            {
                storeMessageLog(this.getInsertId(), message);
            }
        });
    }

    /**
     * Chest transactions
     *
     * @param userId
     * @param location
     * @param itemData
     * @param amount
     * @param containerType
     */
    public void logChestLog(final long userId, final World world, final Location location, final ItemData itemData, final int amount, final int containerType)
    {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        storeLog(world, location, (amount > 0 ? CHEST_PUT : CHEST_TAKE), userId, timestamp, new QueuedLog()
        {
            @Override
            public void run()
            {
                storeChestLog(this.getInsertId(), itemData, amount, containerType);
            }
        });
    }

    private void buildWorldAndLocation(SelectBuilder builder, World world, Location loc1, Location loc2)
    {
        if (world != null)
        {
            builder.field("world_id").isEqual().value(this.module.getCore().getWorldManager().getWorldId(world));
            if (loc1 != null)
            {
                if (loc2 == null) // single location
                {
                    builder.and().field("x").isEqual().value(loc1.getBlockX())
                            .and().field("y").isEqual().value(loc1.getBlockY())
                            .and().field("z").isEqual().value(loc1.getBlockZ());
                }
                else
                // range of locations
                {
                    builder.and().field("x").between(loc1.getBlockX(), loc2.getBlockX())
                            .and().field("y").between(loc1.getBlockY(), loc2.getBlockY())
                            .and().field("z").between(loc1.getBlockZ(), loc2.getBlockZ());
                }
            }
            builder.and();
        }
    }

    private void buildDates(SelectBuilder builder, Timestamp fromDate, Timestamp toDate)
    {
        builder.beginSub().field("date").between(fromDate, toDate).endSub();
    }

    public BlockLookup getBlockLogs(World world, Location loc1, Location loc2,
            Integer[] actions,//blocks only
            Long[] causers, boolean exludeCausers,
            BlockData[] blockdatas, boolean exludeDatas,
            String text, boolean excludeText, //signs only
            Timestamp fromDate, Timestamp toDate)
    {
        boolean hasSign = false;
        for (int action : actions) // Check actions
        {
            switch (action)
            {
                case BLOCK_SIGN:
                    hasSign = true;
                case BLOCK_PLACE:
                case BLOCK_BREAK:
                case BLOCK_CHANGE:
                case BLOCK_GROW_BP:
                case BLOCK_CHANGE_WE:
                case BLOCK_EXPLODE:
                    break;
                default:
                    throw new IllegalStateException("Not a Block-Log!");
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        SelectBuilder sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_block", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        if (actions.length > 0)
        {
            sbuilder.beginSub().field("action").in().valuesInBrackets(actions);
            sbuilder.endSub().and();
        }
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (blockdatas.length > 0)
        {
            if (exludeDatas)
            {
                sbuilder.not();
            }
            sbuilder.beginSub();
            if (blockdatas[0].data == null)
            {
                sbuilder.beginSub()
                        .field("oldBlock").isEqual().value(blockdatas[0].mat.getId()).or()
                        .field("newBlock").isEqual().value(blockdatas[0].mat.getId())
                        .endSub();
            }
            else
            {
                sbuilder.beginSub().beginSub()
                        .field("oldBlock").isEqual().value(blockdatas[0].mat.getId()).and()
                        .field("oldBlockData").isEqual().value(blockdatas[0].data)
                        .endSub().or()
                        .beginSub()
                        .field("newBlock").isEqual().value(blockdatas[0].mat.getId()).and()
                        .field("newBlockData").isEqual().value(blockdatas[0].data)
                        .endSub().endSub();
            }
            for (int i = 1; i < blockdatas.length; ++i)
            {
                sbuilder.or().beginSub();
                if (blockdatas[i].data == null)
                {
                    sbuilder.field("oldBlock").isEqual().value().or()
                            .field("newBlock").isEqual().value();

                }
                else
                {
                    sbuilder.beginSub()
                            .field("oldBlock").isEqual().value().and()
                            .field("oldBlockData").isEqual().value()
                            .endSub().or()
                            .beginSub()
                            .field("newBlock").isEqual().value().and()
                            .field("newBlockData").isEqual().value()
                            .endSub();
                }
                sbuilder.endSub();
            }
            sbuilder.endSub().and();
        }
        this.buildDates(sbuilder, fromDate, toDate);

        String sql = sbuilder.end().end();

        System.out.println("\n\n" + sql); //<---TODO remove this
        BlockLookup lookup = new BlockLookup();
        try
        {
            ResultSet result = this.database.query(sql);
            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                Integer oldBlock = result.getInt("oldBlock");
                Byte oldBlockData = result.getByte("oldBlockData");
                Integer newBlock = result.getInt("newBlock");
                Byte newBlockData = result.getByte("newBlockData");

                World readWorld = this.module.getCore().getWorldManager().getWorld(world_id);
                Location loc = new Location(readWorld, x, y, z);

                lookup.addEntry(new BlockLog(key, action, date, loc, causer,
                        BlockData.get(oldBlock, oldBlockData),
                        BlockData.get(newBlock, newBlockData)));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for block-logs!", ex);
        }
        // SIGN QUERY:
        if (hasSign)
        {
            sbuilder = builder.select().wildcard()
                    .from("log_logs")
                    .joinOnEqual("log_sign", "key", "log_logs", "key").where();
            this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
            sbuilder.field("action").isEqual().value(BLOCK_SIGN).and();
            if (causers.length > 0)
            {
                sbuilder.beginSub();
                if (exludeCausers)
                {
                    sbuilder.not();
                }
                sbuilder.field("causer").in().valuesInBrackets(causers);
                sbuilder.endSub().and();
            }
            if (text != null)
            {
                if (excludeText)
                {
                    sbuilder.not();
                }
                sbuilder.beginSub()
                        .field("oldLine1").like().value("%" + text + "%").or()
                        .field("oldLine2").like().value("%" + text + "%").or()
                        .field("oldLine3").like().value("%" + text + "%").or()
                        .field("oldLine4").like().value("%" + text + "%").or()
                        .field("newLine1").like().value("%" + text + "%").or()
                        .field("newLine2").like().value("%" + text + "%").or()
                        .field("newLine3").like().value("%" + text + "%").or()
                        .field("newLine4").like().value("%" + text + "%").endSub();
            }
            this.buildDates(sbuilder, fromDate, toDate);
            sql = sbuilder.end().end();
            System.out.println("\n\n" + sql); //<---TODO remove this
            try
            {
                ResultSet result = this.database.query(sql);
                while (result.next())
                {
                    Long key = result.getLong("key");
                    Timestamp date = result.getTimestamp("date");
                    Long world_id = result.getLong("world_id");
                    Integer action = result.getInt("action");
                    Integer x = result.getInt("x");
                    Integer y = result.getInt("y");
                    Integer z = result.getInt("z");
                    Long causer = result.getLong("causer");
                    String[] oldLines = new String[4];
                    String[] newLines = new String[4];
                    oldLines[0] = result.getString("oldLine1");
                    oldLines[1] = result.getString("oldLine2");
                    oldLines[2] = result.getString("oldLine3");
                    oldLines[3] = result.getString("oldLine4");

                    newLines[0] = result.getString("newLine1");
                    newLines[1] = result.getString("newLine2");
                    newLines[2] = result.getString("newLine3");
                    newLines[3] = result.getString("newLine4");

                    World readWorld = this.module.getCore().getWorldManager().getWorld(world_id);
                    Location loc = new Location(readWorld, x, y, z);
                    lookup.addEntry(new BlockLog(key, action, date, loc, causer,
                            oldLines, newLines));
                }
            }
            catch (SQLException ex)
            {
                throw new StorageException("Could not execute query for sign-logs!", ex);
            }
        }
        return lookup;
    }

    public KillLookup getKillLogs(World world, Location loc1, Location loc2,
            Integer[] actions,
            Long[] causers, boolean exludeCausers,
            Long[] killed, boolean exludeKilled,
            Timestamp fromDate, Timestamp toDate)
    {
        for (int action : actions) // Check actions
        {
            switch (action)
            {
                case KILL_PVP:
                case KILL_PVE:
                case KILL_EVE:
                case KILL_EVP:
                    break;
                default:
                    throw new IllegalStateException("Not a Kill-Log!");
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        SelectBuilder sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_kill", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        if (actions.length > 0)
        {
            sbuilder.beginSub().field("action").in().valuesInBrackets(actions);
            sbuilder.endSub().and();
        }
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (killed.length > 0)
        {
            sbuilder.beginSub();
            if (exludeKilled)
            {
                sbuilder.not();
            }
            sbuilder.field("killed").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        this.buildDates(sbuilder, fromDate, toDate);

        String sql = sbuilder.end().end();

        System.out.println("\n\n" + sql); //<---TODO remove this
        KillLookup lookup = new KillLookup();
        try
        {
            ResultSet result = this.database.query(sql);

            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                Long readKilled = result.getLong("killed");

                World readWorld = this.module.getCore().getWorldManager().getWorld(world_id);
                Location loc = new Location(readWorld, x, y, z);

                lookup.addEntry(new KillLog(key, action, date, loc, causer, readKilled));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for block-logs!", ex);
        }
        return lookup;
    }

    public MessageLookup getMessageLogs(World world, Location loc1, Location loc2,
            Integer[] actions,
            Long[] causers, boolean exludeCausers,
            String message, boolean excludeMessage,
            Timestamp fromDate, Timestamp toDate)
    {
        for (int action : actions) // Check actions
        {
            switch (action)
            {
                case CHAT:
                case COMMAND:
                    break;
                default:
                    throw new IllegalStateException("Not a Message-Log!");
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        SelectBuilder sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_message", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        if (actions.length > 0)
        {
            sbuilder.beginSub().field("action").in().valuesInBrackets(actions);
            sbuilder.endSub().and();
        }
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (message != null)
        {
            if (excludeMessage)
            {
                sbuilder.not();
            }
            sbuilder.beginSub()
                    .field("message").like().value("%" + message + "%")
                    .endSub();
        }
        this.buildDates(sbuilder, fromDate, toDate);

        String sql = sbuilder.end().end();

        System.out.println("\n\n" + sql); //<---TODO remove this
        MessageLookup lookup = new MessageLookup();
        try
        {
            ResultSet result = this.database.query(sql);
            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                String readmessage = result.getString("message");
                Location location;
                if (world_id == null)
                {
                    location = null;
                }
                else
                {
                    World readWorld = this.module.getCore().getWorldManager().getWorld(world_id);
                    location = new Location(readWorld, x, y, z);
                }
                lookup.addEntry(new MessageLog(key, action, date, location, causer, readmessage));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for block-logs!", ex);
        }
        return lookup;
    }

    public ChestLookup getChestLogs(World world, Location loc1, Location loc2,
            Integer[] actions,
            Long[] causers, boolean exludeCausers,
            ItemData[] datas, boolean excludeData,
            Timestamp fromDate, Timestamp toDate)
    {
        for (int action : actions) // Check actions
        {
            switch (action)
            {
                case CHAT:
                case COMMAND:
                    break;
                default:
                    throw new IllegalStateException("Not a Message-Log!");
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        SelectBuilder sbuilder = builder.select().wildcard()
                .from("log_logs")
                .joinOnEqual("log_chest", "key", "log_logs", "key").where();
        this.buildWorldAndLocation(sbuilder, world, loc1, loc2);
        if (actions.length > 0)
        {
            sbuilder.beginSub().field("action").in().valuesInBrackets(actions);
            sbuilder.endSub().and();
        }
        if (causers.length > 0)
        {
            sbuilder.beginSub();
            if (exludeCausers)
            {
                sbuilder.not();
            }
            sbuilder.field("causer").in().valuesInBrackets(causers);
            sbuilder.endSub().and();
        }
        if (datas.length > 0)
        {
            for (ItemData data : datas)
            {
                if (excludeData)
                {
                    sbuilder.not();
                }
                sbuilder.beginSub()
                        .field("item").isEqual().value(data.mat);
                if (data.data != null)
                {
                    sbuilder.and()
                            .field("data").isEqual().value(data.data);
                }
                if (data.name != null)
                {
                    sbuilder.and()
                            .field("name").like().value("%" + data.name + "%");
                }
                sbuilder.endSub();
            }
        }
        this.buildDates(sbuilder, fromDate, toDate);
        String sql = sbuilder.end().end();

        System.out.println("\n\n" + sql); //<---TODO remove this
        ChestLookup lookup = new ChestLookup();
        try
        {
            ResultSet result = this.database.query(sql);
            while (result.next())
            {
                Long key = result.getLong("key");
                Timestamp date = result.getTimestamp("date");
                Long world_id = result.getLong("world_id");
                Integer action = result.getInt("action");
                Integer x = result.getInt("x");
                Integer y = result.getInt("y");
                Integer z = result.getInt("z");
                Long causer = result.getLong("causer");
                Integer item = result.getInt("item");
                Integer data = result.getInt("data");
                String name = result.getString("name");
                Integer amount = result.getInt("amount");
                Integer containerType = result.getInt("containerType");
                ItemData itemData = new ItemData(item, data.shortValue(), name);
                Location location;
                if (world_id == null)
                {
                    location = null;
                }
                else
                {
                    World readWorld = this.module.getCore().getWorldManager().getWorld(world_id);
                    location = new Location(readWorld, x, y, z);
                }
                lookup.addEntry(new ChestLog(key, action, date, location, causer, itemData, amount, containerType));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not execute query for block-logs!", ex);
        }
        return lookup;
    }

    public void disable()
    {
        CubeEngine.getTaskManager().cancelTask(module, this.repeatingTaskId);
        while (!this.queuedLogs.isEmpty())
        {
            this.doEmptyLogs(logBuffer * 10);
        }
    }
}
