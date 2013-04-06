package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;

/**
 * Water-break
 * <p>Events: {@link WaterFlow}</p>
 */
public class WaterBreak extends BlockActionType
{
    public WaterBreak(Log module)
    {
        super(module, "water-break", BLOCK, ENVIRONEMENT);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot flushed away by water%s!",
                            time ,logEntry.getOldBlock(), loc);
    }
}
