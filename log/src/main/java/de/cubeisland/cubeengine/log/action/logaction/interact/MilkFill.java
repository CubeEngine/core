package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class MilkFill extends LogActionType
{
    public MilkFill(Log module)
    {
        super(module, 0x57, "milk-fill");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &amilked a cow%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
    }
}
