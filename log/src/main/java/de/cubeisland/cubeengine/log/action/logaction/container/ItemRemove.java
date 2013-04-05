package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class ItemRemove extends SimpleLogActionType

{
    public ItemRemove(Log module)
    {
        super(module, 0x91, "item-remove");
    }
}
