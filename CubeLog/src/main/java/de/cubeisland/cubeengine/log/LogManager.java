package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.listeners.Explosion.ExplosionConfig;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.blocks.BlockLog;
import de.cubeisland.cubeengine.log.storage.blocks.BlockLogManager;
import de.cubeisland.cubeengine.log.storage.chests.ChestLog;
import de.cubeisland.cubeengine.log.storage.chests.ChestLogManager;
import de.cubeisland.cubeengine.log.storage.kills.KillLog;
import de.cubeisland.cubeengine.log.storage.kills.KillLogManager;
import de.cubeisland.cubeengine.log.storage.signs.SignChangLog;
import de.cubeisland.cubeengine.log.storage.signs.SignChangeLogManager;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;

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
     * FluidFlow
     *
     * TODO: ActionType detection / stopp logging in the listener if not
     * enabled!
     *
     * MISSING:
     * Chat
     * ConatinerAccess
     * Kill
     * PlayerInteract
     *
     */
    private Map<LogAction, LogListener> loggers = new EnumMap<LogAction, LogListener>(LogAction.class);
    private final Log module;
    private BlockLogManager blockLogManager;
    private SignChangeLogManager signChangeLogManager;
    private KillLogManager killLogManager;
    private ChestLogManager chestLogManager;

    public LogManager(Log module)
    {
        LogListener.initLogManager(this);
        this.module = module;
        this.blockLogManager = new BlockLogManager(module.getDatabase());
        this.signChangeLogManager = new SignChangeLogManager(module.getDatabase());
        this.killLogManager = new KillLogManager(module.getDatabase());
        this.chestLogManager = new ChestLogManager(module.getDatabase());
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
        if (oldState == newState)
        {
            if (oldState != null && newState != null
                && (oldState.getType().equals(newState.getType())
                && oldState.getRawData() == newState.getRawData()))
            {
                return;
            }
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

    public void logKill(DamageCause cause, Entity damager, Entity damagee, Location loc)
    {
        this.killLogManager.store(new KillLog(KillCause.getKillCause(cause, damager), (Player)damager, damagee, loc));
    }

    public void logContainerChange(User user, ItemData data, int amount, Location loc, int type)
    {
        this.chestLogManager.store(new ChestLog(user.getKey(),data,amount,loc,type));
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

    public static enum KillCause
    {
        PLAYER(-1),
        BLAZE(-2),
        CREEPER(-3),
        ENDER_DRAGON(-4),
        ENDERMAN(-4),
        GHAST(-5),
        IRON_GOLEM(-6),
        MAGMA_CUBE(-7),
        SILVER_FISH(-8),
        SKELETON(-9),
        SLIME(-10),
        SPIDER(-11),
        WOLF(-12),
        ZOMBIE(-13),
        ZOMBIE_PIGMAN(-14),
        LIGHTNING(-15),
        FALL_DAMAGE(-16),
        DROWNING(-17),
        SUFFOCATION(-18),
        STARVATION(-19),
        CACTI(-20),
        LAVA(-21),
        POISON(-22), //cant really kill someone in normal minecraft
        WITHER(-23),
        MAGIC(-24),
        OTHER(-25);

        private KillCause(int causeID)
        {
            this.causeID = causeID;
        }
        final private int causeID;

        public int getId()
        {
            return causeID;
        }

        public static KillCause getKillCause(DamageCause cause, Entity damager)
        {
            switch (cause)
            {
                case ENTITY_ATTACK:
                    switch (damager.getType())
                    {
                        case PLAYER:
                            return PLAYER;
                        case BLAZE:
                            return BLAZE;
                        case CREEPER:
                            return CREEPER;
                        case ENDER_DRAGON:
                            return ENDER_DRAGON;
                        case ENDERMAN:
                            return ENDERMAN;
                        case GHAST:
                            return GHAST;
                        case IRON_GOLEM:
                            return IRON_GOLEM;
                        case MAGMA_CUBE:
                            return MAGMA_CUBE;
                        case SILVERFISH:
                            return SILVER_FISH;
                        case SKELETON:
                            return SKELETON;
                        case SLIME:
                            return SLIME;
                        case SPIDER:
                            return SPIDER;
                        case WOLF:
                            return WOLF;
                        case ZOMBIE:
                            return ZOMBIE;
                        case PIG_ZOMBIE:
                            return ZOMBIE_PIGMAN;

                    }
                case LIGHTNING:
                    return LIGHTNING;
                case FALL:
                    return FALL_DAMAGE;
                case DROWNING:
                    return DROWNING;
                case SUFFOCATION:
                    return SUFFOCATION;
                case STARVATION:
                    return STARVATION;
                case CONTACT:
                    return CACTI; // or other blockcontact
                case LAVA:
                    return LAVA;
                case POISON:
                    return POISON;
                case WITHER:
                    return WITHER;
                case MAGIC:
                    return MAGIC;
                default:
                    return OTHER;
            }
        }
    }

    public static enum ContainerType
    {
        CHEST(1),
        FURNACE(2),
        BREWINGSTAND(3),
        DISPENSER(4),
        OTHER(5);
        private final int id;
        private static final TIntObjectHashMap<ContainerType> map;

        static
        {
            map = new TIntObjectHashMap<ContainerType>();
            for (ContainerType type : values())
            {
                map.put(type.id, type);
            }
        }

        private ContainerType(int id)
        {
            this.id = id;
        }

        public static ContainerType getContainerType(int id)
        {
            return map.get(id);
        }

        public static ContainerType getContainerType(Inventory inventory)
        {
            if (inventory.getHolder() instanceof BrewingStand)
            {
                return BREWINGSTAND;
            }
            if (inventory.getHolder() instanceof Chest || inventory.getHolder() instanceof DoubleChest)
            {
                return CHEST;
            }
            if (inventory.getHolder() instanceof Furnace)
            {
                return FURNACE;
            }
            if (inventory.getHolder() instanceof Dispenser)
            {
                return DISPENSER;
            }
            if (inventory.getHolder() instanceof HumanEntity || inventory.getHolder() instanceof StorageMinecart)
            {
                return null;
            }
            return OTHER;
        }

        public int getId()
        {
            return this.id;
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
