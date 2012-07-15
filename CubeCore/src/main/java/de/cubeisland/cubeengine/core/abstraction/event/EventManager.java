package de.cubeisland.cubeengine.core.abstraction.event;

import de.cubeisland.cubeengine.core.abstraction.Plugin;



/**
 *
 * @author CodeInfection
 */
public class EventManager
{
    private static EventManager instance = null;

    private EventManager()
    {
    }

    public static EventManager getInstance()
    {
        if (instance == null)
        {
            instance = new EventManager();
        }
        return instance;
    }

    public EventManager registerEvent(Plugin plugin, Object object)
    {
        // TODO implement
        return this;
    }

    public EventManager unregisterEvents(Object object)
    {
        // TODO implement
        return this;
    }

    public EventManager unregisterEvents(Plugin plugin)
    {
        // TODO implement
        return this;
    }
}
