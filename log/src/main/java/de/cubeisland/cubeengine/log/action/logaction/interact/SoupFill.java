package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class SoupFill extends SimpleLogActionType
{
    public SoupFill(Log module)
    {
        super(module, 0x58, "soup-fill");
    }
}
