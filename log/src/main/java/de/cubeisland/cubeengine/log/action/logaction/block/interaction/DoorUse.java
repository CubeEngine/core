package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class DoorUse extends BlockActionType
{
    public DoorUse(Log module)
    {
        super(module, 0x48, "door-use");
    }
}
