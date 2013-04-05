package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * lighter-ignite
 * <p>Events: {@link IgniteActionType}</p>
 */
public class Lighter extends BlockActionType
{
    public Lighter(Log module)
    {
        super(module, 0x32, "lighter-ignite");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &aset fire%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }
}
