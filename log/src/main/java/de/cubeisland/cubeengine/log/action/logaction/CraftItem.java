package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;

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
}
