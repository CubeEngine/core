package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.user.User;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil
{
    public static boolean giveItemsToUser(User user, ItemStack... items)
    {
        PlayerInventory inventory = user.getInventory();
        ItemStack[] oldInventory = inventory.getContents();
        Map map = inventory.addItem(items);
        if (!map.isEmpty())
        {
            user.getInventory().clear();
            user.getInventory().addItem(oldInventory);
            return false;
        }
        return true;
    }
    
    public static boolean checkForPlace(Inventory inventory, ItemStack... items)
    {
        Inventory inv = Bukkit.createInventory(null, inventory.getSize());
        inv.setContents(inventory.getContents().clone());
        Map map;
        for (ItemStack item : items)
        {
            map = inv.addItem(new ItemStack(item));
            if (!map.isEmpty())
            {
                return false;
            }
        }
        return true;
    }
}