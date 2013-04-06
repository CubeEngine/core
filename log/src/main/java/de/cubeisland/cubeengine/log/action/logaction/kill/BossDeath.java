package de.cubeisland.cubeengine.log.action.logaction.kill;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.KILL;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * boss-death
 * <p>Events: {@link KillActionType}</p>
 */
public class BossDeath extends SimpleLogActionType
{
    public BossDeath(Log module)
    {
        super(module, "boss-death", PLAYER, ENTITY, KILL);
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


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BOSS_DEATH_enable;
    }
}
