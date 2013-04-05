package de.cubeisland.cubeengine.log.action.logaction.interact;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class FireworkUse extends LogActionType
{
    public FireworkUse(Log module)
    {
        super(module, 0x52, "firework-use");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s &aused a firework rocket%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),loc);
    }
}
