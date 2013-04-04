package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class WaterBreak extends BlockActionType
{
    public WaterBreak(Log module)
    {
        super(module, 0x04, "water-break");
    }
}
