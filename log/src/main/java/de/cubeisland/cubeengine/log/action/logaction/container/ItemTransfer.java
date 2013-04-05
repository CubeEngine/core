package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class ItemTransfer extends SimpleLogActionType
{
    public ItemTransfer(Log module)
    {
        super(module, 0x92, "item-transfer");
    }
}
