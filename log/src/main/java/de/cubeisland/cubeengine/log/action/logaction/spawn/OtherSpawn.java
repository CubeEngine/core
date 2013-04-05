package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class OtherSpawn extends SimpleLogActionType

{
    public OtherSpawn(Log module)
    {
        super(module, 0x83, "other-spawn");
    }
}
