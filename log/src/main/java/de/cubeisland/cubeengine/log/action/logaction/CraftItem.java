package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.storage.ActionType.CRAFT_ITEM;
import static de.cubeisland.cubeengine.log.storage.ActionType.ENCHANT_ITEM;

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

}
