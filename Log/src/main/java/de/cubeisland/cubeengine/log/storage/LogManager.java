package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.worker.AsyncTaskQueue;
import de.cubeisland.cubeengine.log.Log;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogManager
{
    //CraftItemEvent
    //enchant item
    //entity break
    //entity dye
    //entity shear
    //entity follow
    //entity spawn
    //fireball

    //hangingitem break
    //hangingitem place

    //item drop
    //item insert
    //item pickup
    //item remove
    //xp pickup

    //EntityChangeBlockEvent / Sheep eat

    //player join
    //player quit

    //splash potion

    //spawnegg use


    //tnt prime / there is no such event yet! https://bukkit.atlassian.net/browse/BUKKIT-45

    //new shiny ones:

    //BREAK etc.
    public static final int BLOCK_BREAK = 0x00;
    public static final int BLOCK_BURN = 0x01;
    public static final int BLOCK_FADE = 0x02;
    public static final int LEAF_DECAY = 0x03;
    public static final int WATER_BREAK = 0x04;
    public static final int LAVA_BREAK = 0x05;
    public static final int ENTITY_BREAK = 0x06;
    public static final int ENDERMAN_PICKUP = 0x07;
    public static final int BUCKET_FILL = 0x08;
    public static final int CROP_TRAMPLE = 0x09;
    //EXPLOSIONS
    public static final int ENTITY_EXPLODE = 0x10;
    public static final int CREEPER_EXPLODE = 0x11;
    public static final int TNT_EXPLODE = 0x12;
    public static final int FIREBALL_EXPLODE = 0x13;
    public static final int ENDERDRAGON_EXPLODE = 0x14;
    public static final int WITHER_EXPLODE = 0x15;
    public static final int TNT_PRIME = 0x16;
    //PLACE etc.
    public static final int BLOCK_PLACE = 0x20;
    public static final int LAVA_BUCKET = 0x21;
    public static final int WATER_BUCKET = 0x22;
    public static final int NATURAL_GROW = 0x23;
    public static final int PLAYER_GROW = 0x24;
    public static final int BLOCK_FORM = 0x25; //ice/snow/lava-water
    public static final int ENDERMAN_PLACE = 0x26;
    public static final int ENTITY_FORM = 0x27;//snow-golem snow
    // SPREAD/ IGNITION
    public static final int FIRE_SPREAD = 0x30;
    public static final int FIREBALL = 0x31;
    public static final int LIGHTER = 0x32;
    public static final int LAVA_IGNITE = 0x33;
    public static final int LIGHTNING = 0x34;
    public static final int BLOCK_SPREAD = 0x35;
    public static final int WATER_FLOW = 0x36;
    public static final int LAVA_FLOW = 0x37;
    //BLOCKCHANGES
    public static final int BLOCK_SHIFT = 0x40; // moved by piston
    public static final int BLOCK_FALL = 0x41;
    public static final int SIGN_CHANGE = 0x42;
    public static final int SHEEP_EAT = 0x43;
    public static final int BONEMEAL_USE = 0x44;
    public static final int LEVER_USE = 0x45;
    public static final int REPEATER_CHANGE = 0x46;
    public static final int NOTEBLOCK_CHANGE = 0x47;
    public static final int DOOR_USE = 0x48;
    public static final int CAKE_EAT = 0x49;
    public static final int WORLDEDIT = 0x4A;
    //INTERACTION (stuff that cannot be rolled back)
    public static final int CONTAINER_ACCESS = 0x50;
    public static final int BUTTON_USE = 0x51;
    public static final int FIREWORK_USE = 0x52;
    public static final int VEHICLE_ENTER = 0x53;
    public static final int VEHICLE_EXIT = 0x54;
    public static final int POTION_SPLASH = 0x55;
    public static final int PLATE_STEP = 0x56;
    //ENTITY-PLACE/BREAK
    public static final int VEHICLE_PLACE = 0x60;
    public static final int HANGING_PLACE = 0x61;
    public static final int VEHICLE_BREAK = 0x62;
    public static final int HANGING_BREAK = 0x63;
    //KILLING
    public static final int PLAYER_KILL = 0x70;
    public static final int ENTITY_KILL = 0x71;
    public static final int PLAYER_DEATH = 0x72;
    //other entity
    public static final int MONSTER_EGG_USE = 0x80;
    public static final int ENTITY_SPAWN = 0x81;
    public static final int ITEM_DROP = 0x82;
    public static final int ITEM_PICKUP = 0x83;
    public static final int XP_PICKUP = 0x84;
    public static final int ENTITY_SHEAR = 0x85;
    public static final int ENTITY_DYE = 0x86;
    //chest-transactions
    public static final int ITEM_INSERT = 0x90;
    public static final int ITEM_REMOVE = 0x91;
    //misc
    public static final int PLAYER_COMMAND = 0xA0;
    public static final int CONSOLE_COMMAND = 0xA1;
    public static final int PLAYER_CHAT = 0xA2;
    public static final int PLAYER_JOIN = 0xA3;
    public static final int PLAYER_QUIT = 0xA4;
    public static final int PLAYER_TELEPORT = 0xA5;
    public static final int ENCHANT_ITEM = 0xA6;
    public static final int CRAFT_ITEM = 0xA7;

//OLD remains:

    public static final int BLOCK_CHANGE = 0x02; //changing blockdata / interactlog
    public static final int BLOCK_SIGN = 0x03; //sign change
    public static final int BLOCK_GROW_BP = 0x04; //growth induced by a player
    public static final int BLOCK_CHANGE_WE = 0x05;
    public static final int BLOCK_EXPLODE = 0x06; // Creeper attacking a player
    public static final int HANGING_ENTITY_PLACE = 0x07; // TODO
    public static final int HANGING_ENTITY_BREAK = 0x08; // TODO

    public static final int KILL_PVP = 0x10; //player killed by player
    public static final int KILL_PVE = 0x11; //player killed by environement
    public static final int KILL_EVE = 0x12; //mob killed by environement
    public static final int KILL_EVP = 0x13; //mob killed by player
    public static final int NATURAL_SPAWN = 0x14; // TODO
    public static final int EGG_SPAWN = 0x15; // TODO
    public static final int CHAT = 0x20;
    public static final int COMMAND = 0x21;
    public static final int CHEST_PUT = 0x30;
    public static final int CHEST_TAKE = 0x31;
    private final AsyncTaskQueue taskQueue = new AsyncTaskQueue(CubeEngine.getTaskManager().getExecutorService());
    private final Database database;
    private final Log module;
    private int logBuffer = 2000; // TODO config
    private final int repeatingTaskId;

    public LogManager(Log module)
    {
        this.database = module.getDatabase();
        this.module = module;
        try
        {
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("log_entries", true).beginFields()
                    .field("key", AttrType.INT, true).autoIncrement()
                    .field("date", AttrType.TIMESTAMP)
                    .field("action", AttrType.TINYINT)
                    .field("world", AttrType.INT, true, false)
                    .field("x", AttrType.INT, false, false)
                    .field("y", AttrType.INT, false, false)
                    .field("z", AttrType.INT, false, false)
                    .field("causer", AttrType.BIGINT, false, false)
                    .field("block",AttrType.VARCHAR, 255, false)
                    .field("data",AttrType.TINYINT,false,false)
                    .field("newBlock",AttrType.VARCHAR, 255, false)
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
        //TODO make this async every second!!!!!!0
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
                        try
                        {
                            doEmptyLogs(logBuffer);
                        }
                        catch (Exception ex)
                        {
                            LogManager.this.module.getLogger().log(LogLevel.ERROR,"Error while logging!",ex);
                        }
                    }
                });
            }
        }, 20, 20);
    }

    private Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<QueuedLog>();
    private volatile boolean running = false;

    private void doEmptyLogs(int amount)
    {
        if (queuedLogs.isEmpty())
        {
            return;
        }
        if (running)
        {
            this.module.getLogger().warning("LogQueue already running! Size: " + queuedLogs.size());
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
        if (logs.size() > 4)
            this.module.getLogger().log(LogLevel.DEBUG,"Logging {0} logs",logs.size());
        long a = System.currentTimeMillis();
        int logSize = logs.size();
        PreparedStatement stmt = this.database.getStoredStatement(this.getClass(),"storeLog");
        try
        {
            for (QueuedLog log : logs)
            {
                log.addDataToBatch(stmt);
            }
            stmt.executeBatch();
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing main Log-Entry", ex, stmt);
        }
        finally
        {
            running = false;
        }
        a = System.currentTimeMillis() - a;
        if (logSize == logBuffer)
            System.out.println("Logged " + logSize + " logs in: " + a / 1000 + "." + a % 1000 + "s | remaining logs: " + queuedLogs.size());
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

    public void disable()
    {
        CubeEngine.getTaskManager().cancelTask(module, this.repeatingTaskId);
        while (!this.queuedLogs.isEmpty())
        {
            this.doEmptyLogs(logBuffer * 10);
        }
    }


    public boolean isIgnored(int blockBreak)
    {
        //TODO config lookup
        return false;
    }

    private void queueLog(Timestamp timestamp, long worldID, int x, int y, int z, int action, Long causer, String block, Byte data, String newBlock, Byte newData, String additionalData)
    {
        this.queuedLogs.offer(new QueuedLog(timestamp,worldID,x,y,z,action,causer,block,data,newBlock,newData,additionalData));
    }

    /**
     * Log a block with additional data
     *
     * @param location
     * @param action
     * @param causer
     * @param block
     * @param data
     * @param newBlock
     * @param newData
     * @param additionalData
     */
    public void queueLog(Location location, int action, Long causer, String block, Byte data, String newBlock, Byte newData, String additionalData)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long worldID = this.module.getCore().getWorldManager().getWorldId(location.getWorld());
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),action, causer, block, data, newBlock, newData,additionalData);
    }

    /**
     * Log with location, player and additional data
     *
     * @param location
     * @param action
     * @param player
     * @param additionalData
     */
    public void queueLog(Location location, int action, Player player, String additionalData)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long worldID = this.module.getCore().getWorldManager().getWorldId(location.getWorld());
        User user = this.module.getUserManager().getExactUser(player);
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),action,user.key,null,null,null,null,additionalData);
    }

}

