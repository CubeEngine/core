package de.cubeisland.cubeengine.log.action.logaction.kill;


import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * npc-death
 * <p>Events: {@link KillActionType}</p>
 */
public class NpcDeath extends SimpleLogActionType

{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY, KILL);
    }

    @Override
    public String getName()
    {
        return "npc-death";
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
        return this.lm.getConfig(world).NPC_DEATH_enable;
    }
}
