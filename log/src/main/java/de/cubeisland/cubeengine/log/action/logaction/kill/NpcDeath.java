package de.cubeisland.cubeengine.log.action.logaction.kill;


import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.KILL;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;

/**
 * npc-death
 * <p>Events: {@link KillActionType}</p>
 */
public class NpcDeath extends SimpleLogActionType

{
    public NpcDeath(Log module)
    {
        super(module, "npc-death", PLAYER, ENTITY, KILL);
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
