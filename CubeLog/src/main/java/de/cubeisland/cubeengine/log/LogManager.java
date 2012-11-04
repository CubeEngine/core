package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.listeners.Explosion.ExplosionConfig;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import de.cubeisland.cubeengine.log.storage.blocks.BlockLog;
import de.cubeisland.cubeengine.log.storage.blocks.BlockLogManager;
import de.cubeisland.cubeengine.log.storage.signs.SignChangLog;
import de.cubeisland.cubeengine.log.storage.signs.SignChangeLogManager;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
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
     * Explosion (misc / what explosion??? when creeper cause of player expl. ->
     * loose info)
     * StructureGrow
     * LeavesDecay
     * SignChange
     * //TODO send the CauseID to the model it does not need no know the player/user etc

     * 
     *
     * TODO: ActionType detection / stopp logging in the listener if not enabled!
     *
     * MISSING:
     * Chat
     * ConatinerAccess
     * FluidFlow
     * Kill
     * PlayerInteract
     * 
     */
    private Map<LogAction, LogListener> loggers = new EnumMap<LogAction, LogListener>(LogAction.class);
    private final Log module;
    private BlockLogManager blockLogManager;
    private SignChangeLogManager signChangeLogManager;

    public LogManager(Log module)
    {
        LogListener.initLogManager(this);
        this.module = module;
        this.blockLogManager = new BlockLogManager(module.getDatabase());
        this.signChangeLogManager = new  SignChangeLogManager(module.getDatabase());
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

    public void logChangeBlock(BlockChangeCause cause, Player player, BlockState oldState, BlockState newState)
    {
        if (oldState == newState || (oldState.getType().equals(newState.getType()) && oldState.getRawData() == newState.getRawData()))
        {
            return;
        }
        if (cause == BlockChangeCause.PLAYER)
        {
            User user = this.module.getUserManager().getExactUser(player);
            this.blockLogManager.store(new BlockLog(user.getKey(), newState, oldState));
        }
        else
        {
            this.blockLogManager.store(new BlockLog(cause, newState, oldState));
        }
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

    public void logSignChange(Player player, String[] lines, BlockState state)
    {
        String[] oldlines = ((Sign)state).getLines();
        for (int i = 0; i < 4; ++i)
        {
            if (lines[0].equals(oldlines[0])
                && lines[1].equals(oldlines[1])
                && lines[2].equals(oldlines[2])
                && lines[3].equals(oldlines[3]))
            {
                return; //No change -> return
            }
        }
        this.signChangeLogManager.store(new SignChangLog(player, state, oldlines, lines));
    }

    public static enum BlockChangeCause
    {
        PLAYER(-1),
        LAVA(-2),
        WATER(-3),
        EXPLOSION(-4),
        FIRE(-5),
        ENDERMAN(-6),
        FADE(-7), FORM(-7),
        DECAY(-8), GROW(-8),
        WITHER(-9);

        private BlockChangeCause(int causeID)
        {
            this.causeID = causeID;
        }
        final private int causeID;

        public int getId()
        {
            return causeID;
        }
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
