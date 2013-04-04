package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class PlateStep extends BlockActionType

{
    public PlateStep(Log module)
    {
        super(module, 0x56, "plate-step");
    }
}
