package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class PlayerDeath extends SimpleLogActionType
{
    public PlayerDeath(Log module)
    {
        super(module, 0x74, "player-death");
    }
}
