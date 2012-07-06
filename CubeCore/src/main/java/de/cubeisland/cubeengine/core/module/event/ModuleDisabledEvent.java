package de.cubeisland.cubeengine.core.module.event;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.event.HandlerList;

/**
 *
 * @author CodeInfection
 */
public class ModuleDisabledEvent extends ModuleEvent
{
    public ModuleDisabledEvent(CubeCore core, Module module)
    {
        super(core, module);
    }


    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
