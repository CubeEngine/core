package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public class ComparatorChange extends BlockActionType
{
    public ComparatorChange(Log module)
    {
        super(module, 0x4A, "comparator-change");
    }
}
