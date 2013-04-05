package de.cubeisland.cubeengine.log.action.logaction.kill;


import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class NpcDeath extends SimpleLogActionType

{
    public NpcDeath(Log module)
    {
        super(module, 0x78, "npc-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        this.manager.getActionType(KillActionType.class).showLogEntry(user, logEntry,time,loc);
    }
}
