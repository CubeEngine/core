package de.cubeisland.cubeengine.log.action.logaction.kill;


import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * other-death
 * <p>Events: {@link KillActionType}</p>
 */
public class OtherDeath extends SimpleLogActionType

{
    public OtherDeath(Log module)
    {
        super(module, 0x7A, "other-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        KillActionType.showSubActionLogEntry(user, logEntry,time,loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }
}
