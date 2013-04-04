package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class CreeperExplode extends BlockActionType
{
    public CreeperExplode(Log module)
    {
        super(module, 0x11, "tnt-explode");
    }
}
