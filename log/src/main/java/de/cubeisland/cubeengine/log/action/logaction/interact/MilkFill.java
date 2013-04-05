package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * filling milk bucket
 * <p>Events: {@link de.cubeisland.cubeengine.log.action.logaction.block.player.BucketFill BucketFill}</p>
 */
public class MilkFill extends SimpleLogActionType
{
    public MilkFill(Log module)
    {
        super(module, 0x57, "milk-fill");
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
