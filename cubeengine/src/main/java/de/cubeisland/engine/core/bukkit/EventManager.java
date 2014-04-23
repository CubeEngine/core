/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.bukkit;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

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
        this.listenerMap = new ConcurrentHashMap<>();
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
            this.listenerMap.put(module, listeners = new THashSet<>(1));
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
        expectNotNull(module, "The module must not be null!");
        expectNotNull(listener, "The listener must not be null!");

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
        expectNotNull(module, "The module must not be null!");

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
