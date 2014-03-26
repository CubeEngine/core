package de.cubeisland.engine.log.action.newaction;

import org.bukkit.World;
import org.bukkit.event.Listener;

public class LogListener implements Listener
{
    public <T extends ActionTypeBase<?>> T newAction(Class<T> clazz, World world)
    {
        // TODO check if actionType is active for given world
        try
        {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
             throw new IllegalArgumentException("Given LogAction cannot be instantiated!");
        }
    }

    public void logAction(ActionTypeBase action)
    {
        // TODO
    }
}
