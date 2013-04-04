package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;

public class ContainerAccess extends LogActionType
{
    public ContainerAccess(Log module)
    {
        super(module, 0x50, "container-access");
    }
}
