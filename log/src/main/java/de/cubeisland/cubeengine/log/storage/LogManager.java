package de.cubeisland.cubeengine.log.storage;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LoggingConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogManager
{
    public final ObjectMapper mapper;
    private final Log module;

    private LoggingConfiguration globalConfig;
    private Map<World, LoggingConfiguration> worldConfigs = new HashMap<World, LoggingConfiguration>();

    private final QueryManager queryManager;

    public LogManager(Log module)
    {
        this.module = module;
        this.mapper = new ObjectMapper();
        File file = new File(module.getFolder(), "worlds");
        file.mkdir();
        this.globalConfig = Configuration.load(LoggingConfiguration.class, new File(module.getFolder(), "globalconfig.yml"));
        for (World world : Bukkit.getServer().getWorlds())
        {
            file = new File(module.getFolder(), "worlds" + File.separator + world.getName());
            file.mkdir();
            this.worldConfigs.put(world, (LoggingConfiguration)globalConfig.loadChild(new File(file, "config.yml")));
        }


        final EventManager em = module.getCore().getEventManager();

        this.queryManager = new QueryManager(module);


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
        this.queryManager.disable();
    }

    public boolean isLogging(World world, ActionType blockBreak, Object additional)
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
            case FIREBALL_IGNITE:
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

    public boolean isIgnored(World world, ActionType action)
    {
        return this.isIgnored(world,action,null);
    }

    public boolean isIgnored(World world, ActionType action, Object additional) {
        return !this.isLogging(world,action,additional);
    }

    /**
     * TODO Remove when WE has its actionType
     */
    public void queueBlockChangeLog(Location location, ActionType action, Long causer, String block, Byte data, String newBlock, Byte newData, String additionalData)
    {
    }

    public int getQueueSize() {
        return this.queryManager.queuedLogs.size();
    }

    public void fillLookupAndShow(final Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user);
    }

    public LoggingConfiguration getConfig(World world) {
        if (world == null) return globalConfig;
        return this.worldConfigs.get(world);
    }

    public void queueLog(QueuedLog log)
    {
        this.queryManager.queueLog(log);
    }
}