package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class EntityExplode extends BlockActionType
{
    public EntityExplode(Log module)
    {
        super(module, 0x10, "tnt-explode");
    }
}
