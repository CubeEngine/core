package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.logger.config.*;
import gnu.trove.map.hash.THashMap;

import java.util.HashMap;
import java.util.Map;

@Codec("yml")
public class LogConfiguration extends Configuration
{
    @Option("enable-logging")
    public boolean enableLogging = true;

    @Option("log-actions")
    public Map<String, LogActionConfig> logActionConfigs = new THashMap<String, LogActionConfig>();

    public Map<Class<? extends LoggerConfig>,LoggerConfig> subConfigs = new HashMap<Class<? extends LoggerConfig>, LoggerConfig>();

    public LogConfiguration()
    {
        this.onLoaded(); // This has to be here to initialize all the logActionConfigs for loading
    }

    @Override
    public void onLoaded()
    {
        LogActionConfig laConfig;
        try
        {
            laConfig = this.addLogActionConfig("BLOCKCHANGE",true);
            this.addLoggerConfig(BlockBreakConfig.class, laConfig)
                .addLoggerConfig(BlockBurnConfig.class, laConfig)
                .addLoggerConfig(BlockDecayConfig.class, laConfig)
                .addLoggerConfig(BlockExplosionConfig.class, laConfig)
                .addLoggerConfig(BlockFadeConfig.class, laConfig)
                .addLoggerConfig(BlockFluidFlowConfig.class, laConfig)
                .addLoggerConfig(BlockFormConfig.class, laConfig)
                .addLoggerConfig(BlockGrowConfig.class, laConfig)
                .addLoggerConfig(BlockPlaceConfig.class, laConfig)
                .addLoggerConfig(EndermanConfig.class, laConfig)
                .addLoggerConfig(InteractionConfig.class, laConfig);
            laConfig = this.addLogActionConfig("SIGNCHANGE",false);
            this.addLoggerConfig(SignChangeConfig.class, laConfig);
            laConfig = this.addLogActionConfig("CONTAINER",true);
            this.addLoggerConfig(ContainerConfig.class, laConfig);
            laConfig = this.addLogActionConfig("CHAT",false);
            this.addLoggerConfig(ChatConfig.class, laConfig);
            laConfig = this.addLogActionConfig("KILL",false);
            this.addLoggerConfig(KillConfig.class, laConfig);
            laConfig = this.addLogActionConfig("WORLDEDIT",false);
            this.addLoggerConfig(WorldEditConfig.class, laConfig);

        } catch (Exception e) {
            throw new InvalidConfigurationException("Invalid LoggerConfigs!",e);
        }
    }

    private LogActionConfig addLogActionConfig(String actionName, boolean defaultEnabled)
    {
        if (this.logActionConfigs.containsKey(actionName))
        {
            for (LoggerConfig loggerConfig: this.logActionConfigs.get(actionName).loggerConfigs.values())
            {
                this.subConfigs.put(loggerConfig.getClass(),loggerConfig);
            }
            return null;
        }
        LogActionConfig logActionConfig = new LogActionConfig();
        logActionConfig.enabled = defaultEnabled;
        this.logActionConfigs.put(actionName, logActionConfig);
        return logActionConfig;
    }

    private LogConfiguration addLoggerConfig(Class<? extends LoggerConfig> configClass, LogActionConfig config) throws IllegalAccessException, InstantiationException {
        if (config == null) return this;
        LoggerConfig loggerConfig = configClass.newInstance();
        config.loggerConfigs.put(loggerConfig.getName(), loggerConfig);
        this.subConfigs.put(configClass,loggerConfig);
        return this;
    }

    public <T extends LoggerConfig> T getSubLogConfig(Class<T> configClass)
    {
        return (T)this.subConfigs.get(configClass);
    }



    /*
    SAND / GRAVEL Logging stuff:

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
