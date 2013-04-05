package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class BossDeath extends SimpleLogActionType
{
    public BossDeath(Log module)
    {
        super(module, 0x79, "boss-death");
    }
}
