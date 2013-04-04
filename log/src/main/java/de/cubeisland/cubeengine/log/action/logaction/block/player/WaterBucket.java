package de.cubeisland.cubeengine.log.action.logaction.block.player;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class WaterBucket extends BlockActionType
{
    public WaterBucket(Log module)
    {
        super(module, 0x22, "water-bucket");
    }
}
