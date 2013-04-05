package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class SoupFill extends SimpleLogActionType
{
    public SoupFill(Log module)
    {
        super(module, 0x58, "soup-fill");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &amade soup with a mooshroom!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }
}
