/*
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.cubeengine.libcube.LibCube;
import org.cubeengine.libcube.ModuleManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * This class manages all Event-(Un-)Registration and fires Events.
 */
public class EventManager
{
    private final ConcurrentMap<Class, Set<Object>> listenerMap;
    private final org.spongepowered.api.event.EventManager eventManager;
    private final PluginContainer plugin;
    private ModuleManager mm;

    @Inject
    public EventManager(ModuleManager mm)
    {
        this.mm = mm;
        this.eventManager = Sponge.getEventManager();
        this.listenerMap = new ConcurrentHashMap<>();
        this.plugin = mm.getPlugin(LibCube.class).get();
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
        this.listenerMap.computeIfAbsent(owner, k -> new HashSet<>()).add(listener);
        this.eventManager.registerListeners(this.mm.getPlugin(owner).orElse(this.plugin), listener);
        return this;
    }

    public <T extends Event> EventManager listenUntil(Class owner, Class<T> eventClass, Predicate<T> filter, Predicate<? super T> listener) {
        eventManager.registerListener(this.mm.getPlugin(owner).orElse(this.plugin), eventClass, new UntilEventListener<>(filter, listener));
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

    public void injectListeners(Injector injector, Object instance, List<Field> fields)
    {
        for (Field field : fields)
        {
            try
            {
                field.setAccessible(true);
                Object listener = injector.getInstance(field.getType());
                field.set(instance, listener);
                this.registerListener(instance.getClass(), listener);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }

    }

    private class UntilEventListener<T extends Event> implements EventListener<T> {

        private final Predicate<T> filter;
        private final Predicate<? super T> listener;

        public UntilEventListener(Predicate<T> filter, Predicate<? super T> listener) {
            this.filter = filter;
            this.listener = listener;
        }

        @Override
        public void handle(T event) {
            if (filter.test(event)) {
                if (listener.test(event)) {
                    eventManager.unregisterListeners(this);
                }
            }
        }
    }
}
