package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Uses Bukkit's event API")
public class EventManager
{
    private final PluginManager pm;

    public EventManager(PluginManager pm)
    {
        this.pm = pm;
    }

    public void registerListener(EventListener listener, Module module)
    {
        this.pm.registerEvents(listener, module.getPluginWrapper());
    }

    public void unregisterListener(EventListener listener)
    {
        HandlerList.unregisterAll(listener);
    }

    public void unregisterListener(Module module)
    {
        HandlerList.unregisterAll(module.getPluginWrapper());
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
