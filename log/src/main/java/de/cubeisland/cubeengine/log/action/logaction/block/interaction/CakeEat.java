package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class CakeEat extends BlockActionType
{
    public CakeEat(Log module)
    {
        super(module, 0x49, "cake-eat");
    }
}
