package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class RepeaterChange extends BlockActionType
{
    public RepeaterChange(Log module)
    {
        super(module, 0x46, "repeater-change");
    }
}
