package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class LavaBreak extends BlockActionType
{
    public LavaBreak(Log module)
    {
        super(module, 0x05, "lava-break");
    }
}
