package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class RepeaterChange extends BlockActionType
{
    public RepeaterChange(Log module)
    {
        super(module, 0x46, "repeater-change");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int delay = (logEntry.getNewBlock().data >> 2) + 1;
        user.sendTranslated("%s&2%s &aset the repeater to &6%d &aticks delay%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(), delay,loc);
        // TODO attach (show the actual change no change -> fiddled around but did not change anything)
    }
}
