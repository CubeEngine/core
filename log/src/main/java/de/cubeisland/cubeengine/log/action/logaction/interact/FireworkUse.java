package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.ITEM;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;

/**
 * launching firework-rockets
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.interaction.RightClickActionType RightClickActionType}</p>
 */
public class FireworkUse extends LogActionType
{
    public FireworkUse(Log module)
    {
        super(module, "firework-use", PLAYER, ENTITY, ITEM);//TODO item
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &aused a firework rocket%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        //TODO
        return false;
    }
}
