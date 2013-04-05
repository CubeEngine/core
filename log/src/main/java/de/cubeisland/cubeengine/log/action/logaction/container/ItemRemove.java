package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Removing items from a container
 * <p>Events: {@link ContainerActionType}
 */
public class ItemRemove extends SimpleLogActionType

{
    public ItemRemove(Log module)
    {
        super(module, 0x91, "item-remove");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        ItemData itemData= logEntry.getItemData();
        user.sendTranslated("%s&2%s&a took &6%d %s&a out of &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            itemData.amount,itemData,
                            logEntry.getMaterialFromNewBlock(),loc);
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }
}
