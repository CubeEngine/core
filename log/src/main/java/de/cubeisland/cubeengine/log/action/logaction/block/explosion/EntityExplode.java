package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class EntityExplode extends BlockActionType
{
    public EntityExplode(Log module)
    {
        super(module, 0x10, "tnt-explode");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&aSomething blew up &6%dx %s&a%s!",
                                time, amount, logEntry.getOldBlock(), loc);
        }
        else
        {
            user.sendTranslated("%s&aSomething blew up &6%s&a%s!",
                                time, logEntry.getOldBlock(), loc);
        }
    }
}
