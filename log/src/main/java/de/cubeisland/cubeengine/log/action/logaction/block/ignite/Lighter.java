package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class Lighter extends BlockActionType
{
    public Lighter(Log module)
    {
        super(module, 0x32, "lighter-ignite");
    }
}
