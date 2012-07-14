package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.module.Module;

/**
 *
 * @author Phillip Schichtel
 */
public interface EventManager
{
    public void registerListener(EventListener listener, Module module);
    public void unregisterListener(EventListener listener);
    public void unregisterListener(Module module);
    public void unregisterListener();
    public <T> T fireEvent(T event);
}
