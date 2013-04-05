package de.cubeisland.cubeengine.log.action.logaction.block.player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class WaterBucket extends BlockActionType
{
    public WaterBucket(Log module)
    {
        super(module, 0x22, "water-bucket");
    }
    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAdditional().size()+1;
            user.sendTranslated("&2%s &aemptied &6&d&a water-buckets!",
                                logEntry.getCauserUser().getDisplayName(),amount);
        }
        else
        {
            user.sendTranslated("&2%s &aemptied a water-bucket!",
                                logEntry.getCauserUser().getDisplayName());
        }
    }
}
