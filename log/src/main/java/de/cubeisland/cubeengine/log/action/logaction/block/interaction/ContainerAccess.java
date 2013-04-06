package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.CANNOT_ROLLBACK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;


/**
 * Accessing Containers
 * <p>Events: {@link RightClickActionType}</p>
 */
public class ContainerAccess extends BlockActionType
{
    public ContainerAccess(Log module)
    {
        super(module, "container-access", BLOCK, PLAYER, CANNOT_ROLLBACK);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &alooked into a &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            logEntry.getOldBlock(),loc);
    }
}
