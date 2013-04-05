package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class ItemInsert extends SimpleLogActionType
{
    public ItemInsert(Log module)
    {
        super(module, 0x90, "item-insert");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a placed &6%d %s&a into &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            logEntry.getItemData().amount,
                            logEntry.getItemData(),
                            logEntry.getNewBlock(),loc);
    }
}
