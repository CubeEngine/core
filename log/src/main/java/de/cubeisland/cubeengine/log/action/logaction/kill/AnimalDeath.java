package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class AnimalDeath extends SimpleLogActionType
{
    public AnimalDeath(Log module)
    {
        super(module, 0x76, "animal-death");
    }
}
