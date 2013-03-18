package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.worker.AsyncTaskQueue;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogConfiguration;
import de.cubeisland.cubeengine.log.listeners.BlockListener;
import de.cubeisland.cubeengine.log.listeners.ChatListener;
import de.cubeisland.cubeengine.log.listeners.ContainerListener;
import de.cubeisland.cubeengine.log.listeners.EntityListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

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
    public static final int HANGING_BREAK = 0x63; // negative causer -> action-type e.g. BLOCK_BURN -1
    //KILLING
    public static final int PLAYER_KILL = 0x70; // determined by causer ID not saved in DB
    public static final int ENTITY_KILL = 0x71; // determined by causer ID not saved in DB
    public static final int BOSS_KILL = 0x72;
    public static final int ENVIRONMENT_KILL = 0x73; // determined by causer ID not saved in DB
    public static final int PLAYER_DEATH = 0x74;
    public static final int MONSTER_DEATH = 0x75;
    public static final int ANIMAL_DEATH = 0x76;
    public static final int PET_DEATH = 0x77;
    public static final int NPC_DEATH = 0x78;
    public static final int BOSS_DEATH = 0x79;
    public static final int OTHER_DEATH = 0x7A;
    //other entity
    public static final int MONSTER_EGG_USE = 0x80;
    public static final int NATURAL_SPAWN = 0x81;
    public static final int SPAWNER_SPAWN = 0x82;
    public static final int OTHER_SPAWN = 0x82;
    public static final int ITEM_DROP = 0x84;
    public static final int ITEM_PICKUP = 0x85;
    public static final int XP_PICKUP = 0x86;
    public static final int ENTITY_SHEAR = 0x87;
    public static final int ENTITY_DYE = 0x88;
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

    private final AsyncTaskQueue taskQueue = new AsyncTaskQueue(CubeEngine.getTaskManager().getExecutorService());
    private final Database database;
    private final Log module;
    private int logBuffer = 2000; // TODO config

    private final BlockListener blockListener;
    private final ChatListener chatListener;
    private final ContainerListener containerListener;
    private final EntityListener entityListener;

    private final ExecutorService executor;
    private final Runnable runner;
    private Future<?> future = null;

    private LogConfiguration globalConfig;
    private Map<World, LogConfiguration> worldConfigs = new HashMap<World, LogConfiguration>();

    public LogManager(Log module)
    {
        File file = new File(module.getFolder(), "worlds");
        file.mkdir();
        this.globalConfig = Configuration.load(LogConfiguration.class, new File(module.getFolder(), "globalconfig.yml"));
        for (World world : Bukkit.getServer().getWorlds())
        {
            //TODO config to disable logging in the entire world
            file = new File(module.getFolder(), "worlds" + File.separator + world.getName());
            file.mkdir();
            this.worldConfigs.put(world, (LogConfiguration)globalConfig.loadChild(new File(file, "config.yml")));
        }

        this.blockListener = new BlockListener(module,this);
        this.chatListener = new ChatListener(module,this);
        this.containerListener = new ContainerListener(module,this);
        this.entityListener = new EntityListener(module,this);
        module.registerListener(blockListener);
        module.registerListener(chatListener);
        module.registerListener(containerListener);
        module.registerListener(entityListener);

        this.database = module.getDatabase();
        this.module = module;
        try
        {
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable("log_entries", true).beginFields()
                    .field("key", AttrType.INT, true).autoIncrement()
                    .field("date", AttrType.TIMESTAMP)
                    .field("action", AttrType.TINYINT, false)
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
        runner = new Runnable() {
            @Override
            public void run() {
                taskQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doEmptyLogs(logBuffer);
                        } catch (Exception ex) {
                            LogManager.this.module.getLogger().log(LogLevel.ERROR, "Error while logging!", ex);
                        }
                    }
                });
            }
        };
        executor = Executors.newSingleThreadExecutor(this.module.getTaskManger().getThreadFactory());
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
            throw new StorageException("Error while storing main Log-Entry", ex, stmt);
        }
        finally
        {
            running = false;
        }
        long nanos = Profiler.endProfiling("logging");
        timeSpend += nanos;
        logsLogged += logSize;
        if (logSize == logBuffer)
        {
            timeSpendFullLoad += nanos;
            logsLoggedFullLoad += logSize;
        }
        if (logSize > 20)
        {
            this.module.getLogger().log(LogLevel.DEBUG,
                    logSize + " logged in: " + TimeUnit.NANOSECONDS.toMillis(nanos) +
                            "ms | remaining logs: " + queuedLogs.size());
            this.module.getLogger().log(LogLevel.DEBUG,
                    "Average logtime per log: " + TimeUnit.NANOSECONDS.toMicros(timeSpend / logsLogged)+ " micros");
            this.module.getLogger().log(LogLevel.DEBUG,
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

    private long timeSpend = 0;
    private long logsLogged = 1;

    private long timeSpendFullLoad = 0;
    private long logsLoggedFullLoad = 1;

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

    private CountDownLatch latch = null;

    public void disable()
    {
        if (!queuedLogs.isEmpty())
        {
            latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                this.module.getLogger().log(LogLevel.WARNING,"Error while waiting!",e);
            }
        }
    }


    public boolean isIgnored(int blockBreak)
    {
        //TODO config lookup
        return false;
    }

    private void queueLog(Timestamp timestamp, Long worldID, Integer x, Integer y, Integer z, Integer action, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        this.queuedLogs.offer(new QueuedLog(timestamp,worldID,x,y,z,action,causer,block,data,newBlock,newData,additionalData));
        if (this.future == null || this.future.isDone())
        {
            this.future = executor.submit(runner);
        }
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
        Long longData = data == null ? null : data.longValue();
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),action, causer, block, longData, newBlock, newData,additionalData);
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

    public void queueLog(int action, String addionalData)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.queueLog(timestamp,null,null,null,null,action, null, null,null,null,null,addionalData);
    }

    public void queueLog(Location location, int action, Long causer, Long killed, String additionalData)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long worldID = this.module.getCore().getWorldManager().getWorldId(location.getWorld());
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),
                action,causer,null,killed,null,null,additionalData);
    }

    public void queueLog(Location location, int action, long causer)
    {
        this.queueLog(location,action,causer,null,null);
    }

    public void queueLog(Location location, int action, Long causer, String material, Short data, String additionalData)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long worldID = this.module.getCore().getWorldManager().getWorldId(location.getWorld());
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),
                action,causer,material,data.longValue(),null,null,additionalData);
    }

    public BlockListener getBlockListener() {
        return blockListener;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public ContainerListener getContainerListener() {
        return containerListener;
    }

    public EntityListener getEntityListener() {
        return entityListener;
    }
}

