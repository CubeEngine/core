package de.cubeisland.cubeengine.log.action.logaction;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ITEM;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * enchanting items
 * <p>Events: {@link EnchantItemEvent}</p>
 */
public class EnchantItem extends SimpleLogActionType
{
    public EnchantItem(Log module)
    {
        super(module, "enchant-item",true, PLAYER,ITEM);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event)
    {
        if (this.isActive(event.getEnchanter().getWorld()))
        {
            ItemData itemData = new ItemData(event.getItem());
            if (itemData.enchantments == null)
            {
                itemData.enchantments = new HashMap<Enchantment, Integer>();
            }
            itemData.enchantments.putAll(event.getEnchantsToAdd());
            this.logSimple(event.getEnchanter(),itemData.serialize(this.om));
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a enchanted &6%s%s&a!",
                           time, logEntry.getCauserUser().getDisplayName(),
                            logEntry.getItemData(),loc);//TODO list enchantments
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return false; //TODO how to attach if?
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENCHANT_ITEM_enable;
    }
}
