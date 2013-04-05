package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class SheepEat extends BlockActionType
{
    public SheepEat(Log module)
    {
        super(module, 0x43, "sheep-eat");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aA sheep ate all the grass%s&a!",time,loc);
    }
}
