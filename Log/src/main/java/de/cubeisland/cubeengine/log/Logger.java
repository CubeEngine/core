package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.event.Listener;

public abstract class Logger<T extends SubLogConfig> implements Listener
{
    protected Log module;
    protected LogManager lm;
    protected T config;
    public final LogAction action;

    public Logger(LogAction action)
    {
        this.module = Log.getInstance();
        this.lm = module.getLogManager();
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
            this.module.registerListener(this);
        }
        else
        {
            this.module.unregisterListener(this);
        }
    }
}
