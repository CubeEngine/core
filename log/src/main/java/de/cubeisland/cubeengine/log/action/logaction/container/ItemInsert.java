package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class ItemInsert extends SimpleLogActionType
{
    public ItemInsert(Log module)
    {
        super(module, 0x90, "item-insert");
    }
}
