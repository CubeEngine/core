package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitEventRegistration implements EventRegistration
{
    private final PluginManager pm;

    public BukkitEventRegistration(PluginManager pm)
    {
        this.pm = pm;
    }

    public void register(Object listener, Module module)
    {
        if (!(listener instanceof Listener))
        {
            throw new IllegalArgumentException("The listener must be an instance of Listener!");
        }
        this.pm.registerEvents((Listener)listener, (Plugin)module.getPluginWrapper());
    }

    public void unregister(Object listener)
    {
        if (!(listener instanceof Listener))
        {
            throw new IllegalArgumentException("The listener must be an instance of Listener!");
        }
        HandlerList.unregisterAll((Listener)listener);
    }

    public void unregister(Module module)
    {
        HandlerList.unregisterAll((Plugin)module.getPluginWrapper());
    }

    public void unregister()
    {
        HandlerList.unregisterAll();
    }
}
