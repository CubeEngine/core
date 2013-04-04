package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;

public class FireworkUse extends LogActionType
{
    public FireworkUse(Log module)
    {
        super(module, 0x52, "firework-use");
    }
}
