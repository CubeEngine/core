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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.InventoryUtil;

public class BackpackInventory implements InventoryHolder
{
    private final Backpack module;
    protected BackpackData data;

    private Map<Player, Integer> viewers = new HashMap<>();
    private Map<Integer, Inventory> views = new HashMap<>();
    
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
            inventory = Bukkit.createInventory(this, data.size * 9, pageString + (index + 1) + "/" + this.data.pages);
            this.views.put(index, inventory);
        }
        ItemStack[] contents = new ItemStack[data.size * 9];
        int offset = index * data.size * 9;
        for (int i = 0; i < data.size * 9; i++)
        {
            contents[i] = data.contents.get(i + offset);
        }
        inventory.setContents(contents);
        return inventory;
    }

    private void saveData(int index, Inventory inventory)
    {
        ItemStack[] contents = inventory.getContents();
        int offset = index * data.size * 9;
        for (int i = 0; i < data.size * 9; i++)
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
        if (index == null || inv == null)
        {
            return;
        }
        this.saveData(index, inv);
        if (inv.getViewers().isEmpty() || (inv.getViewers().size() == 1 && inv.getViewers().get(0) == player))
        {
            this.views.remove(index);
        }
    }

    public void addItem(ItemStack toGive)
    {
        for (Inventory inventory : new ArrayList<>(this.views.values()))
        {
            for (HumanEntity humanEntity : new ArrayList<>(inventory.getViewers()))
            {
                this.saveData(this.viewers.remove((Player)humanEntity), inventory);
                humanEntity.closeInventory();
            }
        }

        LinkedList<ItemStack> itemStacks = new LinkedList<>(Arrays.asList(InventoryUtil.splitIntoMaxItems(toGive, toGive.getMaxStackSize())));
        for (int i = 0 ; itemStacks.size() > 0; i++)
        {
            if (this.data.contents.get(i) == null)
            {
                this.data.contents.put(i, itemStacks.poll());
                if (i > this.data.pages * data.size * 9)
                {
                    this.data.pages = this.data.pages + 1;
                }
            }
        }
        this.data.save();
    }
}
