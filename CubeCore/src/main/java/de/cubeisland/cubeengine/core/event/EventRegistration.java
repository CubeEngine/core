package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.module.Module;

/**
 *
 * @author Phillip Schichtel
 */
public interface EventRegistration
{
    public void register(Object listener, Module module);
    public void unregister(Object listener);
    public void unregister(Module module);
    public void unregister();
}
