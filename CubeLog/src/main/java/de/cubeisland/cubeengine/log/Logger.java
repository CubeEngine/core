package de.cubeisland.cubeengine.log;

import org.bukkit.event.Listener;

public abstract class Logger<T extends SubLogConfig> implements Listener
{
    public static final Log module = Log.getInstance();
    protected T             config;
    public final LogAction  action;

    public Logger(LogAction action)
    {
        this.action = action;
    }

    public T getConfig()
    {
        return this.config;
    }

    public void applyConfig(SubLogConfig config)
    {
        this.config = (T)config;
        this.applyConfig();
    }

    public void applyConfig()
    {
        if (this.config.enabled)
        {
            module.registerListener(this);
        }
        else
        {
            module.unregisterListener(this);
        }
    }
}
