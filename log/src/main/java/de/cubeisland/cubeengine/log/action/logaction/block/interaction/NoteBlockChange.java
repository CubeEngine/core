package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class NoteblockChange extends BlockActionType
{
    public NoteblockChange(Log module)
    {
        super(module, 0x47, "noteblock-change");
    }
}
