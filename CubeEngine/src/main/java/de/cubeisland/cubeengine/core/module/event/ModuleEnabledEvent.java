package de.cubeisland.cubeengine.core.module.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.HandlerList;

//TODO DOCU
public class ModuleEnabledEvent extends ModuleEvent
{
    private static final HandlerList handlers = new HandlerList();

    public ModuleEnabledEvent(Core core, Module module)
    {
        super(core, module);
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