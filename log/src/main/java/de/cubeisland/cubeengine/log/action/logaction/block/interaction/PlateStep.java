package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;


/**
 * Stepping on PressurePlates
 * <p>Events: {@link RightClickActionType}</p>
 */
public class PlateStep extends BlockActionType

{
    public PlateStep(Log module)
    {
        super(module, 0x56, "plate-step");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &astepped on a &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            logEntry.getOldBlock(),loc);
    }
}
