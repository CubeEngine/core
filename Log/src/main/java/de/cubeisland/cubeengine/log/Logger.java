package de.cubeisland.cubeengine.log;

import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public abstract class Logger<T extends LoggerConfig> implements Listener
{
    protected final Log module;
    protected Map<World, T> configs;
    private Class<T> configClass;

    public Logger(Log module, Class<T> configClass)
    {
        this.configClass = configClass;
        this.module = module;
        this.configs = new HashMap<World, T>();
        this.reloadAndApplyConfigs();
    }

    public T getConfig(World world)
    {
        return this.configs.get(world);
    }

    public void reloadAndApplyConfigs()
    {
        boolean enabled = false;
        for (Map.Entry<World, LogConfiguration> worldConfig : module.getConfigurations().entrySet())
        {
            T subLogConfig = worldConfig.getValue().getSubLogConfig(configClass);
            if (subLogConfig.enabled)
            {
                enabled = true;
            }
            this.configs.put(worldConfig.getKey(), subLogConfig);
        }
        if (enabled)
        {
            //if (!(this instanceof WorldEditLogger))
              //  this.module.registerListener(this);
        }
        else
        {
            this.module.removeListener(this);
        }
    }

    public Class<T> getConfigClass()
    {
        return configClass;
    }
}
