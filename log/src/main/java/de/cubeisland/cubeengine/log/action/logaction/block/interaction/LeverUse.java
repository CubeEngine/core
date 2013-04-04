package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class LeverUse extends BlockActionType
{
    public LeverUse(Log module)
    {
        super(module, 0x45, "lever-use");
    }
}
