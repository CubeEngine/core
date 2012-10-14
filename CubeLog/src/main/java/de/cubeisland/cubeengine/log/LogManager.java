package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.listeners.Explosion;
import de.cubeisland.cubeengine.log.listeners.Explosion.ExplosionConfig;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import de.cubeisland.cubeengine.log.storage.BlockLog;
import de.cubeisland.cubeengine.log.storage.BlockLogManager;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LogManager
{
/*
 * TODOne
 * LISTENERS for:
 * BlockBreak (sand stuff)
 * BlockBurn
 * BlockFade
 * BlockForm
 * BlockPlace (sand stuff)
 * Enderman place&break
 * Explosion (misc / what explosion??? when creeper cause of player expl. -> loose info)
 * 
 * 
 * MISSING:
 * Chat
 * ConatinerAccess
 * FluidFlow
 * Kill
 * LeavesDecay
 * PlayerInteract
 * SignChange
 * StructureGrow 
 */
    
    private Map<LogAction, LogListener> loggers = new EnumMap<LogAction, LogListener>(LogAction.class);
    private final Log module;
    private BlockLogManager blockLogManager;

    public LogManager(Log module)
    {
        LogListener.initLogManager(this);
        this.module = module;
        this.blockLogManager = new BlockLogManager(module.getDatabase());
        for (LogSubConfiguration config : module.getConfiguration().configs.values())
        {
            this.registerLogger(config.listener); // register all loaded & enabled Listener 
        }
    }

    private void registerLogger(LogListener logger)
    {
        if (logger.getConfiguration().enabled)
        {
            boolean enable = false;
            for (Object s_action : logger.getConfiguration().actions.keySet())
            {
                LogAction action = LogAction.valueOf((String)s_action);
                if (logger.getConfiguration().actions.get(s_action))
                {
                    loggers.put(action, logger);
                    enable = true;
                }
            }
            if (enable) // if no LogAction was enabled to not register the listener!
            {
                this.module.registerListener(logger);
            }
        }
    }

    public void logBreakBlock(BlockChangeCause cause, Player player, BlockState state)
    {
        this.logChangeBlock(cause, player, state, null);
    }

    public void logChangeBlock(BlockChangeCause cause, Player player, BlockState oldState, BlockState newState)
    {
        if (cause == BlockChangeCause.PLAYER)
        {
            this.blockLogManager.store(new BlockLog(System.currentTimeMillis(), player, newState, oldState));
        }
        else
        {
            this.blockLogManager.store(new BlockLog(cause, System.currentTimeMillis(), newState, oldState));
        }
    }

    public void logEnderGrief(BlockState oldState, BlockState newState)
    {
        this.logChangeBlock(BlockChangeCause.ENDERMAN, null, oldState, newState);
    }

    public void logExplosion(List<Block> blockList, Entity entity)
    {
        Player player = null;
        if (((ExplosionConfig)this.module.getConfiguration().getConfiguration(LogAction.EXPLOSION_CREEPER)).logAsPlayer)
        {
            if (entity.getType().equals(EntityType.CREEPER))
            {
                final Entity target = ((Creeper)entity).getTarget();
                player = target instanceof Player ? ((Player)target) : null;
            }
        }
        for (Block block : blockList)
        {
            if (player == null)
            {
                this.logChangeBlock(BlockChangeCause.EXPLOSION, player, block.getState(), null);
            }
            else
            {
                this.logChangeBlock(BlockChangeCause.PLAYER, player, block.getState(), null); //TODO this is not ideal ! information about explosion is gone :(
            }
        }
    }

    public void logPlaceBlock(Player player, BlockState placedBlock, BlockState replacedBlock)
    {
        this.logChangeBlock(BlockChangeCause.PLAYER, player, replacedBlock, placedBlock);
         /* //TODO SAND etc.
         * if (placedBlock.getType() == Material.SAND || placedBlock.getType()
         * == Material.GRAVEL || placedBlock.getType() == Material.DRAGON_EGG)
         * {
         * Location finalLoc = placedBlock.getLocation();
         * int y = finalLoc.getBlockY();
         * while (this.canFall(finalLoc))
         * {
         * }
         * switch (finalLoc.getBlock().getType())
         * {
         * case STEP:
         * case WOOD_STEP:
         * case CAKE_BLOCK:
         * case DIODE_BLOCK_ON:
         * case DIODE_BLOCK_OFF:
         * case TRAP_DOOR:
         * case TORCH:
         * case SIGN:
         * case SIGN_POST:
         * case PORTAL:
         * case RED_ROSE:
         * case YELLOW_FLOWER:
         * case RED_MUSHROOM:
         * case BROWN_MUSHROOM:
         * case SAPLING:
         * case CROPS:
         * case ENDER_PORTAL:
         * case STONE_BUTTON:
         * case LEVER:
         * case TRIPWIRE_HOOK:
         * case TRIPWIRE:
         * case STONE_PLATE:
         * case WOOD_PLATE:
         * case REDSTONE_TORCH_OFF:
         * case REDSTONE_TORCH_ON:
         * case SUGAR_CANE_BLOCK:
         * case MELON_STEM:
         * case PUMPKIN_STEM:
         * case VINE:
         * case NETHER_WARTS:
         * case WATER_LILY:
         * if (y - 1 == finalLoc.getBlockY())
         * {
         * finalLoc.add(0, 1, 0); // Block did not fall!
         * }
         * else
         * {
         * // TODO trapdoor sand does not break if opened!
         * finalLoc = null; // Block was falling and will break
         * }
         * }
         * } */
    }

    public static enum BlockChangeCause
    {
        PLAYER, FIRE, ENDERMAN, EXPLOSION, FADE, FORM
    }

    //TODO move this into Util
    private boolean canFall(Location loc)
    {
        Material mat = loc.getWorld().getBlockAt(loc.add(0, -1, 0)).getType();
        if (loc.getY() == 0)
        {
            return false;
        }
        switch (mat)
        {
            //fall
            case AIR:
            //fall and place
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case SNOW:
            case LONG_GRASS:
            //fall and or break
            case STEP:
            case WOOD_STEP:
            case CAKE_BLOCK:
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
            case TRAP_DOOR:
            case TORCH:
            case SIGN:
            case SIGN_POST:
            case PORTAL:
            case RED_ROSE:
            case YELLOW_FLOWER:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case SAPLING:
            case CROPS:
            case ENDER_PORTAL:
            case STONE_BUTTON:
            case LEVER:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case SUGAR_CANE_BLOCK:
            case MELON_STEM:
            case PUMPKIN_STEM:
            case VINE:
            case NETHER_WARTS:
                //TODO add 1.4 blocks
                //case WOOD_BUTTON: 
                return true;
            default: //else block
                loc.add(0, 1, 0); //is solid so cannot fall more
                return false;
        }
    }
}
