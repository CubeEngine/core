package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class NaturalSpawn extends SimpleLogActionType

{
    public NaturalSpawn(Log module)
    {
        super(module, 0x81, "natural-spawn");
    }
}
