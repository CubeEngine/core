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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.plugin.PluginContainer;

/**
 * This class manages all Event-(Un-)Registration and fires Events.
 */
public class EventManager
{
    private final org.spongepowered.api.event.EventManager em;
    private final PluginContainer plugin;

    @Inject
    public EventManager(Game game, PluginContainer plugin)
    {
        this.em = game.eventManager();
        this.plugin = plugin;
    }

    /**
     * Registers an event listener with a module
     *
     * @param listener the listener
     * @return fluent interface
     */
    public EventManager registerListener(Object listener)
    {
        this.em.registerListeners(this.plugin, listener);
        return this;
    }

    public <T extends Event> EventManager listenUntil(Class<?> owner, Class<T> eventClass, Predicate<T> filter, Predicate<? super T> listener) {

        final EventListenerRegistration<T> registration = EventListenerRegistration.builder(eventClass)
                                                                                   .plugin(this.plugin)
                                                                                   .listener(new UntilEventListener<>(filter, listener))
                                                                                   .build();
        em.registerListener(registration);
        return this;
    }

    /**
     * Removes an event listener from a module
     *
     * @param listener the listener
     * @return fluent interface
     */
    public EventManager removeListener(Object listener)
    {
        em.unregisterListeners(listener);
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
        this.em.post(event);
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
                this.registerListener(listener);
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
                    em.unregisterListeners(this);
                }
            }
        }
    }
}
