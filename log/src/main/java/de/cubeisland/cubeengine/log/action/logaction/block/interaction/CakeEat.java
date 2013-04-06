package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;

/**
 * Eating cake
 * <p>Events: {@link RightClickActionType}</p>
 */
public class CakeEat extends BlockActionType
{
    public CakeEat(Log module)
    {
        super(module, "cake-eat", BLOCK, PLAYER);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int piecesLeft = 6 - logEntry.getNewBlock().data;
        if (piecesLeft == 0)
        {
            user.sendTranslated("%s&aThe cake is a lie%s&a! Ask &2%s &ahe knows it!",
                                time,loc,logEntry.getCauserUser().getDisplayName());
        }
        else
        {
            user.sendTranslated("%s&2%s &aate a piece of cake%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
    }
}
