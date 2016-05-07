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
package org.cubeengine.libcube.service.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ModularityHandler;
import de.cubeisland.engine.modularity.core.PostInjectionHandler;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.plugin.PluginContainer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class manages all Event-(Un-)Registration and fires Events.
 */
@ServiceProvider(EventManager.class)
public class EventManager implements ModularityHandler, PostInjectionHandler<ModuleListener>
{
    private final ConcurrentMap<Class, Set<Object>> listenerMap;
    private final org.spongepowered.api.event.EventManager eventManager;
    private final Object plugin;

    @Inject
    public EventManager(Game game, Modularity modularity)
    {
        this.eventManager = game.getEventManager();
        this.listenerMap = new ConcurrentHashMap<>();
        this.plugin = modularity.provide(PluginContainer.class).getInstance().get();

        modularity.registerHandler(this);
    }

    /**
     * Registers an event listener with a module
     *
     * @param owner   the module
     * @param listener the listener
     * @return fluent interface
     */
    public EventManager registerListener(Class owner, Object listener)
    {
        Set<Object> listeners = this.listenerMap.get(owner);
        if (listeners == null)
        {
            this.listenerMap.put(owner, listeners = new HashSet<>());
        }
        listeners.add(listener);

        eventManager.registerListeners(plugin, listener);
        return this;
    }

    /**
     * Removes an event listener from a module
     *
     * @param owner   the module
     * @param listener the listener
     * @return fluent interface
     */
    public EventManager removeListener(Class owner, Object listener)
    {
        checkNotNull(owner, "The module must not be null!");
        checkNotNull(listener, "The listener must not be null!");

        Set<Object> listeners = this.listenerMap.get(owner);
        if (listeners != null && listeners.remove(listener))
        {
            eventManager.unregisterListeners(listener);
        }
        return this;
    }

    /**
     * Removes all listeners of the given module
     *
     * @param owner te module
     */
    private void removeListeners(Class owner)
    {
        Set<Object> listeners = this.listenerMap.remove(owner);
        if (listeners != null)
        {
            listeners.forEach(eventManager::unregisterListeners);
        }
    }

    /**
     * Removes all listeners registered by the CubeEngine
     *
     * @return fluent interface
     */
    public EventManager removeListeners()
    {
        Iterator<Entry<Class, Set<Object>>> it = this.listenerMap.entrySet().iterator();
        while (it.hasNext())
        {
            it.next().getValue().forEach(eventManager::unregisterListeners);
            it.remove();
        }
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
        this.eventManager.post(event);
        return event;
    }

    @Override
    public void onEnable(Object instance)
    {
    }

    @Override
    public void handle(ModuleListener annotation, Object injected, Object owner)
    {
        registerListener(owner.getClass(), injected);
    }

    @Override
    public void onDisable(Object instance)
    {
        removeListeners(instance.getClass());
    }
}
