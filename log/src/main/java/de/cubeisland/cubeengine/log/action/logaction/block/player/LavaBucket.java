package de.cubeisland.cubeengine.log.action.logaction.block.player;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class LavaBucket  extends BlockActionType

{
    public LavaBucket(Log module)
    {
        super(module, 0x21, "lava-bucket");
    }
}
