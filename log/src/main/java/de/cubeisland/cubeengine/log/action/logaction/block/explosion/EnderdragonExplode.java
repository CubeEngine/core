package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class EnderdragonExplode extends BlockActionType
{
    public EnderdragonExplode(Log module)
    {
        super(module, 0x14, "tnt-explode");
    }
}
