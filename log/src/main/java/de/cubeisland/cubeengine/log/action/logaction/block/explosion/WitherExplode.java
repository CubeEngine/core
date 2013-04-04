package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class WitherExplode extends BlockActionType
{
    public WitherExplode(Log module)
    {
        super(module, 0x15, "tnt-explode");
    }
}
