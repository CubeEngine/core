package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class manages all Event-(Un-)Registration and fires Events.
 */
public class EventManager
{
    private final BukkitCore corePlugin;
    private final PluginManager pm;
    private final ConcurrentMap<Module, Set<Listener>> listenerMap;

    public EventManager(Core core)
    {
        this.corePlugin = (BukkitCore)core;
        this.pm = this.corePlugin.getServer().getPluginManager();
        this.listenerMap = new ConcurrentHashMap<Module, Set<Listener>>();
    }

    /**
     * Registers an event listener with a module
     *
     * @param module   the module
     * @param listener the listener
     * @return fluent interface
     */
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

    /**
     * Removes an event listener from a module
     *
     * @param module   the module
     * @param listener the listener
     * @return fluent interface
     */
    public EventManager removeListener(Module module, Listener listener)
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

    /**
     * Removes all listeners of the given module
     *
     * @param module te module
     * @return fluent interface
     */
    public EventManager removeListeners(Module module)
    {
        Validate.notNull(module, "The module must not be null!");

        Set<Listener> listeners = this.listenerMap.remove(module);
        if (listeners != null)
        {
            for (Listener listener : listeners)
            {
                HandlerList.unregisterAll(listener);
            }
        }
        return this;
    }

    /**
     * Removes all listeners registered by the CubeEngine
     *
     * @return fluent interface
     */
    public EventManager removeListeners()
    {
        Iterator<Entry<Module, Set<Listener>>> it = this.listenerMap.entrySet().iterator();
        while (it.hasNext())
        {
            for (Listener listener : it.next().getValue())
            {
                HandlerList.unregisterAll(listener);
            }
            it.remove();
        }
        HandlerList.unregisterAll(this.corePlugin);
        return this;
    }

    /**
     * Fires an event
     *
     * @param <T>   the event type
     * @param event the event instance
     * @return the event instance
     */
    public <T extends Event> T fireEvent(T event)
    {
        this.pm.callEvent(event);
        return event;
    }
}
