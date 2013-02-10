package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryUtil
{
    public static boolean giveItemsToUser(User user, ItemStack... items)
    {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack item : items)
        {
            if (item.getAmount() > 64)
            {
                int amount = item.getAmount();
                item.setAmount(64);
                while (amount > 64)
                {
                    ItemStack itemToAdd = item.clone();
                    list.add(itemToAdd);
                    amount -= 64;
                }
                if (amount > 0)
                {
                    item.setAmount(amount);
                    list.add(item);
                }
            }
            else
            {
                list.add(item);
            }
        }
        items = list.toArray(new ItemStack[list.size()]);
        PlayerInventory inventory = user.getInventory();
        ItemStack[] oldInventory = inventory.getContents();
        Map map = inventory.addItem(items);
        if (!map.isEmpty())
        {
            user.getInventory().setContents(oldInventory);
            return false;
        }
        return true;
    }

    public static boolean checkForPlace(Inventory inventory, ItemStack... items)
    {
        Inventory inv =
            inventory.getSize() <= 27
            ? Bukkit.createInventory(null, inventory.getType())
            : Bukkit.createInventory(null, inventory.getSize());

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

    /**
     * Returns the amount of this itemstack in the given inventory
     *
     * @param inventory
     * @param itemStack
     * @return
     */
    public static int getAmountOf(Inventory inventory, ItemStack itemStack)
    {
        int amount = 0;
        for (ItemStack i : inventory.getContents()) {
            if (itemStack.isSimilar(i)) {
                amount += i.getAmount();
            }
        }
        return amount;
    }
}
