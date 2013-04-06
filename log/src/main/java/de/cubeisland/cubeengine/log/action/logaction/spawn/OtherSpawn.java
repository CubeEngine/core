package de.cubeisland.cubeengine.log.action.logaction.spawn;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;

/**
 * other spawning (by player)
 * <p>Events: {@link EntitySpawnActionType}</p>
 */
public class OtherSpawn extends SimpleLogActionType

{
    public OtherSpawn(Log module)
    {
        super(module, "other-spawn", true, ENTITY);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {//TODO get player in data once possible
        user.sendTranslated("%s&6%s &aspawned%s&a!",
                           time, logEntry.getCauserEntity(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world
            && logEntry.location.equals(other.location);
    }
}
