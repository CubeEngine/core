package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;

public class MilkFill extends LogActionType
{
    public MilkFill(Log module)
    {
        super(module, 0x57, "milk-fill");
    }
}
