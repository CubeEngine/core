package de.cubeisland.cubeengine.log.action.logaction.kill;


import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class OtherDeath extends SimpleLogActionType

{
    public OtherDeath(Log module)
    {
        super(module, 0x7A, "other-death");
    }
}
