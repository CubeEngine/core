package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.CubeEvent;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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

    public EventManager registerListener(Listener listener, Module module)
    {
        //TODO this.pm.registerEvents(listener, module.getPluginWrapper());
        this.pm.registerEvents(listener, (Plugin)module.getCore());
        return this;
    }

    public EventManager unregisterListener(Listener listener)
    {
        HandlerList.unregisterAll(listener);
        return this;
    }

    public EventManager unregisterListener(Module module)
    {
        HandlerList.unregisterAll(module.getPluginWrapper());
        return this;
    }

    public EventManager unregisterListener()
    {
        HandlerList.unregisterAll();
        return this;
    }

    public <T extends CubeEvent> T fireEvent(T event)
    {
        this.pm.callEvent(event);
        return event;
    }
}