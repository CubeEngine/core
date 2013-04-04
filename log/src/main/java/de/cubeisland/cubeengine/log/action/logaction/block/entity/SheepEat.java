package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class SheepEat extends BlockActionType
{
    public SheepEat(Log module)
    {
        super(module, 0x43, "sheep-eat");
    }
}
