package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class PetDeath extends SimpleLogActionType
{
    public PetDeath(Log module)
    {
        super(module, 0x77, "pet-death");
    }
}
