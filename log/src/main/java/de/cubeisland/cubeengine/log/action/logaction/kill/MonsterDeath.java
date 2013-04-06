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
 * monster-death
 * <p>Events: {@link KillActionType}</p>
 */
public class MonsterDeath extends SimpleLogActionType
{
    public MonsterDeath(Log module)
    {
        super(module, "monster-death", PLAYER, ENTITY, KILL);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        KillActionType.showSubActionLogEntry(user, logEntry,time,loc);;
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).MONSTER_DEATH_enable;
    }
}
