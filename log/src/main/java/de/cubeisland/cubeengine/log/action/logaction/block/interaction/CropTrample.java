package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;


/**
 * Trampling Crops
 * <p>Events: {@link RightClickActionType}</p>
 */
public class CropTrample extends BlockActionType

{
    public CropTrample(Log module)
    {
        super(module, 0x09, "crop-trample");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        // TODO attached log only show the crop trampled down then
        user.sendTranslated("&2%s &atrampeled down &6%s&a!",
                            logEntry.getCauserUser().getDisplayName(),
                            logEntry.getOldBlock());
    }
}
