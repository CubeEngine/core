package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitEventManager implements EventManager
{
    private final PluginManager pm;

    public BukkitEventManager(PluginManager pm)
    {
        this.pm = pm;
    }

    public void registerListener(EventListener listener, Module module)
    {
        this.pm.registerEvents((Listener)listener, (Plugin)module.getPluginWrapper());
    }

    public void unregisterListener(EventListener listener)
    {
        HandlerList.unregisterAll((Listener)listener);
    }

    public void unregisterListener(Module module)
    {
        HandlerList.unregisterAll((Plugin)module.getPluginWrapper());
    }

    public void unregisterListener()
    {
        HandlerList.unregisterAll();
    }

    public <T> T fireEvent(T event)
    {
        if (!(event instanceof Event))
        {
            throw new IllegalArgumentException("The event must be a Bukkit event");
        }
        this.pm.callEvent((Event)event);
        return event;
    }
}
