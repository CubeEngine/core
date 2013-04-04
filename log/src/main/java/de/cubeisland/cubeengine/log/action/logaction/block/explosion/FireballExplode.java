package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class FireballExplode extends BlockActionType
{
    public FireballExplode(Log module)
    {
        super(module, 0x13, "tnt-explode");
    }
}
