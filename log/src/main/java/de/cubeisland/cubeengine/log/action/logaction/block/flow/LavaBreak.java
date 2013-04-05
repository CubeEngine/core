package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class LavaBreak extends BlockActionType
{
    public LavaBreak(Log module)
    {
        super(module, 0x05, "lava-break");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &agot destroyed by lava%s!",
                            time,logEntry.getOldBlock(),loc);
    }
}
