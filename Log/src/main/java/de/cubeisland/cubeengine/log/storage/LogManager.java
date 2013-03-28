package de.cubeisland.cubeengine.log.storage;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
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
import de.cubeisland.cubeengine.log.LoggingConfiguration;
import de.cubeisland.cubeengine.log.listeners.BlockListener;
import de.cubeisland.cubeengine.log.listeners.ChatListener;
import de.cubeisland.cubeengine.log.listeners.ContainerListener;
import de.cubeisland.cubeengine.log.listeners.EntityListener;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogManager
{
    //BREAK
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
    public static final int OTHER_IGNITE = 0x38;
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
    public static final int COMPARATOR_CHANGE = 0x4A;
    public static final int WORLDEDIT = 0x4B;
    //INTERACTION (stuff that cannot be rolled back)
    public static final int CONTAINER_ACCESS = 0x50;
    public static final int BUTTON_USE = 0x51;
    public static final int FIREWORK_USE = 0x52;
    public static final int VEHICLE_ENTER = 0x53;
    public static final int VEHICLE_EXIT = 0x54;
    public static final int POTION_SPLASH = 0x55;
    public static final int PLATE_STEP = 0x56;
    public static final int MILK_FILL = 0x57;
    public static final int SOUP_FILL = 0x58;
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
    public static final int OTHER_SPAWN = 0x83;
    public static final int ITEM_DROP = 0x84;
    public static final int ITEM_PICKUP = 0x85;
    public static final int XP_PICKUP = 0x86;
    public static final int ENTITY_SHEAR = 0x87;
    public static final int ENTITY_DYE = 0x88;
    //chest-transactions
    public static final int ITEM_INSERT = 0x90;
    public static final int ITEM_REMOVE = 0x91;
    public static final int ITEM_TRANSFER = 0x92;

    public static final int ITEM_CHANGE_IN_CONTAINER = 0x93; // this ID is not used in the database
    //misc
    public static final int PLAYER_COMMAND = 0xA0;
    public static final int CONSOLE_COMMAND = 0xA1;
    public static final int PLAYER_CHAT = 0xA2;
    public static final int PLAYER_JOIN = 0xA3;
    public static final int PLAYER_QUIT = 0xA4;
    public static final int PLAYER_TELEPORT = 0xA5;
    public static final int ENCHANT_ITEM = 0xA6;
    public static final int CRAFT_ITEM = 0xA7;

    private final ExecutorService executorService;
    private final AsyncTaskQueue taskQueue;
    private final Database database;
    private final Log module;
    private final int batchSize;

    private final BlockListener blockListener;
    private final ChatListener chatListener;
    private final ContainerListener containerListener;
    private final EntityListener entityListener;

    private final ExecutorService executor;
    private final Runnable runner;
    private Future<?> future = null;

    private LoggingConfiguration globalConfig;
    private Map<World, LoggingConfiguration> worldConfigs = new HashMap<World, LoggingConfiguration>();

    public final ObjectMapper mapper;

    public LogManager(Log module)
    {
        this.batchSize = module.getConfiguration().loggingBatchSize;
        this.mapper = module.getObjectMapper();
        File file = new File(module.getFolder(), "worlds");
        file.mkdir();
        this.globalConfig = Configuration.load(LoggingConfiguration.class, new File(module.getFolder(), "globalconfig.yml"));
        for (World world : Bukkit.getServer().getWorlds())
        {
            file = new File(module.getFolder(), "worlds" + File.separator + world.getName());
            file.mkdir();
            this.worldConfigs.put(world, (LoggingConfiguration)globalConfig.loadChild(new File(file, "config.yml")));
        }

        this.blockListener = new BlockListener(module,this);
        this.chatListener = new ChatListener(module,this);
        this.containerListener = new ContainerListener(module,this);
        this.entityListener = new EntityListener(module,this);

        final EventManager em = module.getCore().getEventManager();
        em.registerListener(module, blockListener);
        em.registerListener(module, chatListener);
        em.registerListener(module, containerListener);
        em.registerListener(module, entityListener);

        this.database = module.getCore().getDB();
        this.module = module;
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

        this.executorService = Executors.newSingleThreadScheduledExecutor(this.module.getCore().getTaskManager().getThreadFactory()); // TODO is not shut down!
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
                            LogManager.this.module.getLog().log(LogLevel.ERROR, "Error while logging!", ex);
                        }
                    }
                });
            }
        };
        executor = Executors.newSingleThreadExecutor(this.module.getCore().getTaskManager().getThreadFactory());
    }

    private Queue<QueuedLog> queuedLogs = new ConcurrentLinkedQueue<QueuedLog>();
    private volatile boolean running = false;

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
                this.module.getLog().log(LogLevel.WARNING,"Error while waiting!",e);
            }
        }
    }

    public boolean isLogging(World world, int blockBreak, Object additional)
    {
        LoggingConfiguration config = this.getConfig(world);
        if (config.enable == false)
        {
            return false;
        }
        switch (blockBreak)
        {
            case BLOCK_BREAK :
                return config.BLOCK_BREAK_enable;
            case BLOCK_BURN :
                return config.BLOCK_BURN_enable;
            case BLOCK_FADE :
                if (additional instanceof Material)
                {
                    if (additional.equals(Material.ICE))
                    {
                        return config.BLOCK_FADE_ice;
                    }
                    else if (additional.equals(Material.SNOW))
                    {
                        return config.BLOCK_FADE_snow;
                    }
                    else
                    {
                        return config.BLOCK_FADE_other;
                    }
                }
                throw new IllegalStateException("Invalid BLOCK_FADE:" +additional);
            case LEAF_DECAY :
                return config.LEAF_DECAY_enable;
            case WATER_BREAK :
                return config.WATER_BREAK_enable;
            case LAVA_BREAK :
                return config.LAVA_BREAK_enable;
            case ENTITY_BREAK :
                return config.ENTITY_BREAK_enable;
            case ENDERMAN_PICKUP :
                return config.ENDERMAN_PICKUP_enable;
            case BUCKET_FILL :
                return config.BUCKET_FILL_enable;
            case CROP_TRAMPLE :
                return config.CROP_TRAMPLE_enable;
                //EXPLOSIONS
            case ENTITY_EXPLODE :
                return config.ENTITY_EXPLODE_enable;
            case CREEPER_EXPLODE :
                return config.CREEPER_EXPLODE_enable;
            case TNT_EXPLODE :
                return config.TNT_EXPLODE_enable;
            case FIREBALL_EXPLODE :
                return config.FIREBALL_EXPLODE_enable;
            case ENDERDRAGON_EXPLODE :
                return config.ENDERDRAGON_EXPLODE_enable;
            case WITHER_EXPLODE :
                return config.WITHER_EXPLODE_enable;
            case TNT_PRIME :
                return config.TNT_PRIME_enable;
                //PLACE etc.
            case BLOCK_PLACE :
                return config.BLOCK_PLACE_enable;
            case LAVA_BUCKET :
                return config.LAVA_BUCKET_enable;
            case WATER_BUCKET :
                return config.WATER_BUCKET_enable;
            case NATURAL_GROW :
                return config.NATURAL_GROW_enable;
            case PLAYER_GROW :
                return config.PLAYER_GROW_enable;
            case BLOCK_FORM : //ice/snow/lava-water
                if (additional instanceof Material)
                {
                    if (additional.equals(Material.ICE))
                    {
                        return config.BLOCK_FORM_ice;
                    }
                    else if (additional.equals(Material.SNOW))
                    {
                        return config.BLOCK_FORM_snow;
                    }
                    else if (additional.equals(Material.COBBLESTONE)
                        || additional.equals(Material.STONE)
                        || additional.equals(Material.OBSIDIAN))
                    {
                        return config.BLOCK_FORM_lavaWater;
                    }
                    else
                    {
                        return config.BLOCK_FORM_other;
                    }
                }
                throw new IllegalStateException("Invalid BLOCK_FORM:" +additional);
            case ENDERMAN_PLACE :
                return config.ENDERMAN_PLACE_enable;
            case ENTITY_FORM ://snow-golem snow
                return config.ENTITY_FORM_enable;
                // SPREAD/ IGNITION
            case FIRE_SPREAD :
                return config.FIRE_SPREAD_enable;
            case FIREBALL :
                return config.FIREBALL_enable;
            case LIGHTER :
                return config.LIGHTER_enable;
            case LAVA_IGNITE :
                return config.LAVA_IGNITE_enable;
            case LIGHTNING :
                return config.LIGHTNING_enable;
            case BLOCK_SPREAD :
                return config.BLOCK_SPREAD_enable;
            case WATER_FLOW :
                return config.WATER_FLOW_enable;
            case LAVA_FLOW :
                return config.LAVA_FLOW_enable;
            case OTHER_IGNITE :
                return config.OTHER_IGNITE_enable;
                //BLOCKCHANGES
            case BLOCK_SHIFT : // moved by piston
                return config.BLOCK_SHIFT_enable;
            case BLOCK_FALL :
                return config.BLOCK_FALL_enable;
            case SIGN_CHANGE :
                return config.SIGN_CHANGE_enable;
            case SHEEP_EAT :
                return config.SHEEP_EAT_enable;
            case BONEMEAL_USE :
                return config.BONEMEAL_USE_enable;
            case LEVER_USE :
                return config.LEVER_USE_enable;
            case REPEATER_CHANGE :
                return config.REPEATER_CHANGE_enable;
            case NOTEBLOCK_CHANGE :
                return config.NOTEBLOCK_CHANGE_enable;
            case DOOR_USE :
                return config.DOOR_USE_enable;
            case CAKE_EAT :
                return config.CAKE_EAT_enable;
            case COMPARATOR_CHANGE :
                return config.COMPARATPR_CHANGE_enable;
            case WORLDEDIT :
                return config.WORLDEDIT_enable;
                //INTERACTION (stuff that cannot be rolled back)
            case CONTAINER_ACCESS :
                return config.CONTAINER_ACCESS_enable;
            case BUTTON_USE :
                return  config.BUTTON_USE_enable;
            case FIREWORK_USE :
                return  config.FIREWORK_USE_enable;
            case VEHICLE_ENTER :
                return  config.VEHICLE_ENTER_enable;
            case VEHICLE_EXIT :
                return  config.VEHICLE_EXIT_enable;
            case POTION_SPLASH :
                return  config.POTION_SPLASH_enable;
            case PLATE_STEP :
                return  config.PLATE_STEP_enable;
                //ENTITY-PLACE/BREAK
            case VEHICLE_PLACE :
                return  config.VEHICLE_PLACE_enable;
            case HANGING_PLACE :
                return  config.HANGING_PLACE_enable;
            case VEHICLE_BREAK :
                return  config.VEHICLE_BREAK_enable;
            case HANGING_BREAK : // negative causer -> action-type e.g. BLOCK_BURN -1
                return  config.HANGING_BREAK_enable;
                //KILLING
            case PLAYER_KILL : // determined by causer ID not saved in DB
                return  config.PLAYER_KILL_enable;
            case ENTITY_KILL : // determined by causer ID not saved in DB
                return  config.ENTITY_KILL_enable;
            case BOSS_KILL :
                return  config.BOSS_KILL_enable;
            case ENVIRONMENT_KILL : // determined by causer ID not saved in DB
                return  config.ENVIRONMENT_KILL_enable;
            case PLAYER_DEATH :
                return  config.PLAYER_DEATH_enable;
            case MONSTER_DEATH :
                return  config.MONSTER_DEATH_enable;
            case ANIMAL_DEATH :
                return  config.ANIMAL_DEATH_enable;
            case PET_DEATH :
                return  config.PET_DEATH_enable;
            case NPC_DEATH :
                return  config.NPC_DEATH_enable;
            case BOSS_DEATH :
                return  config.BOSS_DEATH_enable;
            case OTHER_DEATH :
                return  config.OTHER_DEATH_enable;
                //other entity
            case MONSTER_EGG_USE :
                return  config.MONSTER_EGG_USE_enable;
            case NATURAL_SPAWN :
                return  config.NATURAL_SPAWN_enable;
            case SPAWNER_SPAWN :
                return  config.SPAWNER_SPAWN_enable;
            case OTHER_SPAWN :
                return  config.OTHER_SPAWN_enable;
            case ITEM_DROP :
                return  config.ITEM_DROP_enable;
            case ITEM_PICKUP :
                return  config.ITEM_PICKUP_enable;
            case XP_PICKUP :
                return  config.XP_PICKUP_enable;
            case ENTITY_SHEAR :
                return  config.ENTITY_SHEAR_enable;
            case ENTITY_DYE :
                return  config.ENTITY_DYE_enable;
                //chest-transactions
            case ITEM_INSERT :
                return  config.ITEM_INSERT_enable;
            case ITEM_REMOVE :
                return config.ITEM_REMOVE_enable;
            case ITEM_TRANSFER :
                return config.ITEM_TRANSFER_enable;
            case ITEM_CHANGE_IN_CONTAINER:
                if (additional instanceof InventoryHolder)
                {
                    if (additional instanceof Chest || additional instanceof DoubleChest)
                    {
                        return config.containerChest;
                    }
                    if (additional instanceof Dispenser)
                        return config.containerDispenser;
                    if (additional instanceof Furnace)
                        return config.containerFurnace;
                    if (additional instanceof BrewingStand)
                        return config.containerBrewingstand;
                    if (additional instanceof StorageMinecart)
                        return config.containerMinecart;
                    if (additional instanceof Hopper)
                        return config.containerHopper;
                    if (additional instanceof Dropper)
                        return config.containerDropper;
                    if (additional instanceof HumanEntity){
                        return false; // no need to log these
                    }
                    this.module.getLog().log(LogLevel.DEBUG,"Unknown InventoryHolder: "+ additional);
                    return false;
                }
                if (additional == null)
                {
                    this.module.getLog().log(LogLevel.DEBUG, "Inventory has no InventoryHolder!");
                    return false;
                }
                throw new IllegalStateException("Invalid ITEM_CHANGE_IN_CONTAINER: "+additional);
            case PLAYER_COMMAND :
                if (additional instanceof String)
                {
                    for (String ignore : config.PLAYER_COMMAND_ignoreRegex)
                    {
                        if (((String)additional).matches(ignore))
                        {
                            return false;
                        }
                    }
                    return  config.PLAYER_COMMAND_enable;
                }
                throw new IllegalStateException("Invalid command message! PLAYER_COMMAND");
            case CONSOLE_COMMAND :
                return  config.CONSOLE_COMMAND_enable;
            case PLAYER_CHAT :
                return  config.PLAYER_CHAT_enable;
            case PLAYER_JOIN :
                return  config.PLAYER_JOIN_enable;
            case PLAYER_QUIT :
                return  config.PLAYER_QUIT_enable;
            case PLAYER_TELEPORT :
                return  config.PLAYER_TELEPORT_enable;
            case ENCHANT_ITEM :
                return  config.ENCHANT_ITEM_enable;
            case CRAFT_ITEM :
                return  config.CRAFT_ITEM_enable;
            case MILK_FILL:
                return config.BUCKET_FILL_milk;
            case SOUP_FILL:
                return config.BOWL_FILL_SOUP;
        }
        return false;
    }

    public boolean isIgnored(World world, int action)
    {
        return this.isIgnored(world,action,null);
    }

    public boolean isIgnored(World world, int action, Object additional) {
        return !this.isLogging(world,action,additional);
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
        User user = this.module.getCore().getUserManager().getExactUser(player);
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

    public LoggingConfiguration getConfig(World world) {
        if (world == null) return globalConfig;
        return this.worldConfigs.get(world);
    }

    public void queueLog(Location location, int action, Long causer, Material material, Short dura, String containerType, String additional)
    {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long worldID = this.module.getCore().getWorldManager().getWorldId(location.getWorld());
        this.queueLog(timestamp,worldID,location.getBlockX(),location.getBlockY(),location.getBlockZ(),
                action,causer,material.name(),dura.longValue(),containerType,null,additional);
    }


    public int getQueueSize() {
        return this.queuedLogs.size();
    }
}

