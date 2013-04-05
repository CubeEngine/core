package de.cubeisland.cubeengine.log.action.logaction;

import java.util.HashMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;

import static de.cubeisland.cubeengine.log.storage.ActionType.ENCHANT_ITEM;

/**
 * Created with IntelliJ IDEA.
 * User: Anselm
 * Date: 05.04.13
 * Time: 02:48
 * To change this template use File | Settings | File Templates.
 */
public class EnchantItem extends SimpleLogActionType
{
    public EnchantItem(Log module)
    {
        super(module, 0xA6, "enchant-item");
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
}
