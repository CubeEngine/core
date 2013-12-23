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
package de.cubeisland.engine.backpack;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.InventoryGuardFactory;

public class BackpackInventory implements InventoryHolder
{
    private final Backpack module;
    private final BackpackData data;

    private Map<Player, Integer> viewers = new HashMap<>();
    private Map<Integer, Inventory> views = new HashMap<>();
    
    public static final int SIZE = 9*6;
    private static final String pageString = ChatFormat.parseFormats("&6Page ");

    public BackpackInventory(Backpack module, BackpackData data)
    {
        this.module = module;
        this.data = data;
    }

    @Override
    /**
     * Always returns the first inventory page
     */
    public Inventory getInventory()
    {
        return this.getInventory(0);
    }

    public void openInventory(Player player)
    {
        this.openInventory(0, player);
    }

    private Inventory getInventory(int index)
    {
        Inventory inventory = this.views.get(index);
        if (inventory == null)
        {
            inventory = Bukkit.createInventory(this, SIZE, pageString + (index + 1));
            this.views.put(index, inventory);
        }
        ItemStack[] contents = new ItemStack[SIZE];
        int offset = index * SIZE;
        for (int i = 0; i < SIZE; i++)
        {
            contents[i] = data.contents.get(i + offset);
        }
        inventory.setContents(contents);
        return inventory;
    }

    private void saveData(int index, Inventory inventory)
    {
        ItemStack[] contents = inventory.getContents();
        int offset = index * SIZE;
        for (int i = 0; i < SIZE; i++)
        {
            if (contents[i] == null)
            {
                data.contents.remove(i + offset);
            }
            else
            {
                data.contents.put(i + offset, contents[i]);
            }
        }
        data.save();
    }

    private void openInventory(int index, Player player)
    {
        if (player instanceof User)
        {
            player = player.getPlayer();
        }
        this.viewers.put(player, index);
        if (data.allowItemsIn)
        {
            player.openInventory(this.getInventory(index));
        }
        else
        {
            InventoryGuardFactory.prepareInventory(this.getInventory(index),
               module.getCore().getUserManager().getExactUser(player.getName())).
                blockPutInAll().submitInventory(module, true);
        }
    }

    public void showNextPage(Player player)
    {
        Integer index = viewers.get(player);
        if (data.pages == 1)
        {
            return;
        }
        player.closeInventory();
        int newIndex;
        if (index == data.pages -1)
        {
            newIndex = 0;
        }
        else
        {
            newIndex = index + 1;
        }
        this.openInventory(newIndex, player);
    }

    public void showPrevPage(Player player)
    {
        Integer index = viewers.get(player);
        if (data.pages == 1)
        {
            return;
        }
        player.closeInventory();
        int newIndex;
        if (index == 0)
        {
            newIndex = data.pages - 1;
        }
        else
        {
            newIndex = index - 1;
        }
        this.openInventory(newIndex, player);
    }

    public void closeInventory(Player player)
    {
        Integer index = viewers.remove(player);
        Inventory inv = views.get(index);
        this.saveData(index, inv);
        if (inv.getViewers().isEmpty())
        {
            this.views.remove(index);
        }
    }
}
