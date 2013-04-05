package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class AnimalDeath extends SimpleLogActionType
{
    public AnimalDeath(Log module)
    {
        super(module, 0x76, "animal-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        this.manager.getActionType(KillActionType.class).showLogEntry(user, logEntry,time,loc);
    }
}
