package de.cubeisland.cubeengine.core.module.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.CubeEvent;
import org.bukkit.event.HandlerList;

public class FinishedLoadModulesEvent extends CubeEvent
{
    private static final HandlerList handlers = new HandlerList();

    public FinishedLoadModulesEvent(Core core)
    {
        super(core);
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
