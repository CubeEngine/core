package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * filling milk bucket
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.player.BucketFill BucketFill}</p>
 */
public class MilkFill extends SimpleLogActionType
{
    public MilkFill(Log module)
    {
        super(module, "milk-fill", true, PLAYER, ENTITY);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world;
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &amilked a cow%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }
}
