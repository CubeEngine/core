package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class ButtonUse extends BlockActionType
{
    public ButtonUse(Log module)
    {
        super(module, 0x51, "button-use");
    }
}
