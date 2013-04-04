package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class TntExplode extends BlockActionType
{
    public TntExplode(Log module)
    {
        super(module, 0x12, "tnt-explode");
    }
}
