package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class MonsterDeath extends SimpleLogActionType
{
    public MonsterDeath(Log module)
    {
        super(module, 0x75, "monster-death");
    }
}
