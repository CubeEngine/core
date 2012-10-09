package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.set.hash.THashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class EventManager
{
    private final BukkitCore corePlugin;
    private final PluginManager pm;
    private final Map<Module, Set<Listener>> listenerMap;

    public EventManager(Core core)
    {
        this.corePlugin = (BukkitCore)core;
        this.pm = this.corePlugin.getServer().getPluginManager();
        this.listenerMap = new ConcurrentHashMap<Module, Set<Listener>>();
    }

    public EventManager registerListener(Module module, Listener listener)
    {
        Set<Listener> listeners = this.listenerMap.get(module);
        if (listeners == null)
        {
            this.listenerMap.put(module, listeners = new THashSet<Listener>(1));
        }
        listeners.add(listener);
        
        this.pm.registerEvents(listener, this.corePlugin);
        return this;
    }

    public EventManager unregisterListener(Module module, Listener listener)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(listener, "The listener must not be null!");
        
        Set<Listener> listeners = this.listenerMap.get(module);
        if (listeners != null && listeners.remove(listener))
        {
            HandlerList.unregisterAll(listener);
        }
        return this;
    }

    public EventManager unregisterListener(Module module)
    {
        Validate.notNull(module, "The module must not be null!");
        
        Set<Listener> listeners = this.listenerMap.get(module);
        if (listeners != null)
        {
            for (Listener listener : listeners)
            {
                HandlerList.unregisterAll(listener);
            }
            this.listenerMap.clear();
        }
        return this;
    }

    public EventManager unregisterListener()
    {
        for (Set<Listener> listeners : this.listenerMap.values())
        {
            for (Listener listener : listeners)
            {
                HandlerList.unregisterAll(listener);
            }
        }
        HandlerList.unregisterAll(this.corePlugin);
        return this;
    }

    public <T extends CubeEvent> T fireEvent(T event)
    {
        this.pm.callEvent(event);
        return event;
    }
}