package de.cubeisland.cubeengine.log.action.logaction.kill;


import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class NpcDeath extends SimpleLogActionType

{
    public NpcDeath(Log module)
    {
        super(module, 0x78, "npc-death");
    }
}
