package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Using doors
 * <p>Events: {@link RightClickActionType}</p>
 */
public class DoorUse extends BlockActionType
{
    public DoorUse(Log module)
    {
        super(module, "door-use", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (!((logEntry.getOldBlock().data & 0x4) == 0x4))
        {
            user.sendTranslated("%s&2%s &aopened the &6%s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aclosed the &6%s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
    }
}
