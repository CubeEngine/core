package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.logger.*;
import gnu.trove.map.hash.THashMap;

public enum LogAction
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
     * ConatinerAccess
     * Kill
     * PlayerInt
     * Chat
     * Interact
     *
     * TODO: ActionType detection / stopp logging in the listener if not
     * enabled!
     */
    BLOCKCHANGE(
        true,
        BlockBreakLogger.class,
        BlockBurnLogger.class,
        BlockDecayLogger.class,
        BlockExplosionLogger.class,
        BlockFadeLogger.class,
        BlockFluidFlowLogger.class,
        BlockFormLogger.class,
        BlockGrowLogger.class,
        BlockPlaceLogger.class,
        EndermanLogger.class),
    SIGNCHANGE(
        false,
        SignChangeLogger.class),
    CONTAINER(
        true,
        ContainerLogger.class),
    CHAT(
        false,
        ChatLogger.class),
    INTERACTION(
        false,
        InteractionLogger.class),
    KILL(
        false,
        KillLogger.class), ;
    private Class<? extends Logger>[] loggerClasses;
    private LogActionConfig configuration;
    private static THashMap<String, Logger> loggers = new THashMap<String, Logger>();

    private LogAction(boolean defaultEnabled, Class<? extends Logger>... logger)
    {
        this.loggerClasses = logger;
        this.configuration = new LogActionConfig(defaultEnabled);
    }

    public LogActionConfig getConfiguration() // returns the config for this action -> can contain more sublogconfigs
    {
        try
        {

            for (Class<? extends Logger> loggerClass : loggerClasses)
            {
                Logger logger = loggerClass.newInstance();
                configuration.configs.put(logger.getConfig().getName(), logger.getConfig());
                loggers.put(logger.getConfig().getName(), logger);
            }
            return configuration;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Invalid LogActionConfig!", ex);
        }
    }

    public void applyLoadedConfig(LogActionConfig actionConfig)
    {
        for (SubLogConfig config : actionConfig.configs.values())
        {
            loggers.get(config.getName()).applyConfig(config);
        }
    }
    //BlockFormEvent & BlockFadeEvent for SnowCover and Ice
    //
    //BlockIgniteEvent for setting fire with fireball flint_and_steel || ?? lava , lightning , spread
    //BlockPistonEvent or BlockPistonExtendEvent / BlockPistonRetractEvent piston movements ??   
    //BlockFromToEvent ?? water lava dragoneggs
    //BlockGrowEvent ?? for wheat sugarcane cactus watermelon pumpkin
    //BlockPhysicsEvent ?? for when phyics is checked
    //BlockRedstoneEvent ?? when redstone update
    //BrewEvent ??
    //FurnaceBurnEvent ?? when fuel burned
    //FurnaceSmeltEvent ?? when block smelted
    //NotePlayEvent ?? noteblock by player or redstone
    /* FOR SAND:
     * private boolean canFall(Location loc)
     * {
     * Material mat = loc.getWorld().getBlockAt(loc.add(0, -1, 0)).getType();
     * if (loc.getY() == 0)
     * {
     * return false;
     * }
     * switch (mat)
     * {
     * //fall
     * case AIR:
     * //fall and place
     * case WATER:
     * case STATIONARY_WATER:
     * case LAVA:
     * case STATIONARY_LAVA:
     * case SNOW:
     * case LONG_GRASS:
     * //fall and or break
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
     * //TODO add 1.4 blocks
     * //case WOOD_BUTTON:
     * return true;
     * default: //else block
     * loc.add(0, 1, 0); //is solid so cannot fall more
     * return false;
     * }
     * }
     */
}
