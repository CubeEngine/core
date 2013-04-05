package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Items transferred by hoppers or droppers
 * <p>Events: {@link ContainerActionType}
 */
public class ItemTransfer extends SimpleLogActionType
{

    public ItemTransfer(Log module)
    {
        super(module, 0x92, "item-transfer");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s&a got moved out of &6%s%s&a!",
                            time,logEntry.getItemData(),
                            logEntry.getNewBlock(),loc);
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }
}
