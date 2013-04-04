package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class EndermanPickup  extends BlockActionType
{
    public EndermanPickup(Log module)
    {
        super(module, 0x07, "enderman-place");
    }

}
