package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;


/**
 * Flipping levers
 * <p>Events: {@link RightClickActionType}</p>
 */
public class LeverUse extends BlockActionType
{
    public LeverUse(Log module)
    {
        super(module, 0x45, "lever-use");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if ((logEntry.getNewBlock().data & 0x8) == 0x8)
        {
            user.sendTranslated("%s&2%s &aactivated the lever%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("&s&2%s &adeactivated the lever%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(),loc);
        }
    }
}
