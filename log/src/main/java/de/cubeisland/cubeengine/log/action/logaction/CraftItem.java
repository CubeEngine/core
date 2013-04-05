package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * crafting items
 * <p>Events: {@link CraftItemEvent}</p>
 */
public class CraftItem extends SimpleLogActionType
{
    public CraftItem(Log module, int id, String name)
    {
        super(module, 0xA7, "craft-item");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (this.isActive(event.getWhoClicked().getWorld()))
        {
            ItemData itemData = new ItemData(event.getRecipe().getResult());
            this.logSimple(event.getWhoClicked(),itemData.serialize(this.om));
        }
    }
    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a crafted &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            logEntry.getItemData(),loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer == other.causer
            && logEntry.world == other.world
            && logEntry.getItemData().equals(other.getItemData()); // ignoring amount
    }
}
