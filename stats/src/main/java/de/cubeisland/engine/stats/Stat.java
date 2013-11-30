package de.cubeisland.engine.stats;

import java.util.UUID;

import org.bukkit.event.Listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.UserManager;

/**
 * A statistic, for example play time
 */
public abstract class Stat implements Listener
{
    private StatsManager manager;
    private Module owner;
    private Core core;

    public void init(StatsManager manager)
    {
        this.manager = manager;
        this.owner = manager.getModule();
        this.core = owner.getCore();
        core.getEventManager().registerListener(owner, this);
    }

    public void onActivate()
    {}

    public UserManager getUserManager()
    {
        return core.getUserManager();
    }

    /**
     * Write a database entry
     *
     * This just calls StatsManager.save(Stat, Object)
     *
     * @param object The object to write in the entry
     */
    public void save(Object object) throws JsonProcessingException
    {
        manager.save(this, object);
    }

    public abstract String getName();
}
