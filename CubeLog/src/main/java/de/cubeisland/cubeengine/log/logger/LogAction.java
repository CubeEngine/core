package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.logger.blockchange.*;
import de.cubeisland.cubeengine.log.logger.blockchange.kill.KillLogger;
import de.cubeisland.cubeengine.log.logger.container.ContainerLogger;
import de.cubeisland.cubeengine.log.logger.signchange.SignChangeLogger;
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
    BLOCKCHANGE(true,
    BlockBreakLogger.class, BlockBurnLogger.class,
    BlockDecayLogger.class, BlockExplosionLogger.class,
    BlockFadeLogger.class, BlockFluidFlowLogger.class,
    BlockFormLogger.class, BlockGrowLogger.class,
    BlockPlaceLogger.class, EndermanLogger.class),
    SIGNCHANGE(false, SignChangeLogger.class),
    CONTAINER(true, ContainerLogger.class),
    //CHAT(),//TODO
    //INTERACTION(),
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
//BlockBurnEvent for destroyed by fire
//LeavesDecayEvent for natural leaves decay
//BlockIgniteEvent for setting fire with fireball flint_and_steel || ?? lava , lightning , spread
//SignChangeEvent for when sign is changed //TODO find when this is fired perhaps after finished editing the sign
//BlockPistonEvent or BlockPistonExtendEvent / BlockPistonRetractEvent piston movements ??   
//BlockFromToEvent ?? water lava dragoneggs
//BlockGrowEvent ?? for wheat sugarcane cactus watermelon pumpkin
//BlockPhysicsEvent ?? for when phyics is checked
//BlockRedstoneEvent ?? when redstone update
//BrewEvent ??
//FurnaceBurnEvent ?? when fuel burned
//FurnaceSmeltEvent ?? when block smelted
//NotePlayEvent ?? noteblock by player or redstone
    
      /*
     * BLOCKPLACE(BlockPlace.class),
     * PLAYER_CHANGESIGN(SignChange.class),
     * EXPLOSION_TNT(Explosion.class),
     * EXPLOSION_CREEPER(Explosion.class),
     * EXPLOSION_GHASTFIREBALL(Explosion.class),
     * EXPLOSION_ENDERDRAGON(Explosion.class),
     * EXPLOSION_MISC(Explosion.class),
     * NATURAL_FIRE(BlockBurn.class),
     * ENDERMEN(Enderman.class),
     * NATURAL_DECAY(LeavesDecay.class),
     * LAVAFLOW(FluidFlow.class),
     * WATERFLOW(FluidFlow.class),
     * DISPENSERACCESS(ContainerAccess.class),
     * CHESTACCESS(ContainerAccess.class),
     * FURNACEACCESS(ContainerAccess.class),
     * BREWINGSTANDACCESS(ContainerAccess.class),
     * COMMAND(Chat.class),
     * CONSOLE(Chat.class),
     * SNOWFORM(BlockForm.class),
     * SNOWFADE(BlockFade.class),
     * ICEFORM(BlockForm.class),
     * ICEFADE(BlockFade.class),
     * DOORINTERACT(PlayerInteract.class),
     * SWITCHINTERACT(PlayerInteract.class),
     * CAKEEAT(PlayerInteract.class),
     * NOTEBLOCKINTERACT(PlayerInteract.class),
     * DIODEINTERACT(PlayerInteract.class),
     * NATURALSTRUCTUREGROW(StructureGrow.class),
     * BONEMEALSTRUCTUREGROW(StructureGrow.class),
     * KILL(StructureGrow.class),
     * CHAT(StructureGrow.class),; */
}
