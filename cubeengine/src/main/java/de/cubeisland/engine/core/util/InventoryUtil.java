/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.cubeisland.engine.core.user.User;

public class InventoryUtil
{
    public static boolean giveItemsToUser(User user, ItemStack... items)
    {
        List<ItemStack> list = new ArrayList<>();
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

    public static int getMissingSpace(Inventory inventory, ItemStack... items)
    {
        Inventory inv =
                inventory.getSize() <= 27
                        ? Bukkit.createInventory(null, inventory.getType())
                        : Bukkit.createInventory(null, inventory.getSize());
        inv.setContents(inventory.getContents().clone());
        Map<Integer,ItemStack> map;
        int missingPlace = 0;
        for (ItemStack item : items)
        {
            map = inv.addItem(new ItemStack(item));
            for (ItemStack itemNotAdded : map.values())
            {
                missingPlace += itemNotAdded.getAmount();
            }
        }
        return missingPlace;
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
        for (ItemStack i : inventory.getContents())
        {
            if (itemStack.isSimilar(i))
            {
                amount += i.getAmount();
            }
        }
        return amount;
    }

    public static ItemStack[] splitIntoMaxItems(ItemStack item, int maxStackSize)
    {
        int itemAmount = item.getAmount();
        List<ItemStack> list = new ArrayList<>();
        while (itemAmount > maxStackSize)
        {
            itemAmount -= maxStackSize;
            ItemStack itemToAdd = new ItemStack(item);
            itemToAdd.setAmount(maxStackSize);
            list.add(itemToAdd);
        }
        ItemStack itemToAdd = new ItemStack(item);
        itemToAdd.setAmount(itemAmount);
        list.add(itemToAdd);
        return list.toArray(new ItemStack[list.size()]);
    }
}
