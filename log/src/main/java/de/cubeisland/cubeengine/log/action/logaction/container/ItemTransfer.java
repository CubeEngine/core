package de.cubeisland.cubeengine.log.action.logaction.container;

import java.util.EnumSet;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * Items transferred by hoppers or droppers
 * <p>Events: {@link ContainerActionType#onItemMove ContainerAction}
 */
public class ItemTransfer extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, INVENTORY, ITEM);
    }

    @Override
    public String getName()
    {
        return "item-transfer";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s&a got moved out of &6%s%s&a!",
                            time,logEntry.getItemData(),
                            logEntry.getContainerTypeFromBlock(),loc);
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_TRANSFER_enable;
    }
}
