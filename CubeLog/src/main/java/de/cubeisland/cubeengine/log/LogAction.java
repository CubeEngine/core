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
    BLOCKCHANGE(true,
    BlockBreakLogger.class, BlockBurnLogger.class,
    BlockDecayLogger.class, BlockExplosionLogger.class,
    BlockFadeLogger.class, BlockFluidFlowLogger.class,
    BlockFormLogger.class, BlockGrowLogger.class,
    BlockPlaceLogger.class, EndermanLogger.class),
    SIGNCHANGE(false, SignChangeLogger.class),
    CONTAINER(true, ContainerLogger.class),
    CHAT(false, ChatLogger.class),
    INTERACTION(false, InteractionLogger.class),
    KILL(false, KillLogger.class),;
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
}
