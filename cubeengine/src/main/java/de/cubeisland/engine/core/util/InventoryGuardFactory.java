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

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;

public class InventoryGuardFactory
{
    public InventoryGuardFactory(Core core)
    {
        this.core = core;
    }

    private final Core core;
    private ThreadLocal<InventoryGuard> currentGuardConfig;

    public static InventoryGuardFactory prepareInventory(Inventory inventory, User... users)
    {
        return CubeEngine.getCore().getInventoryGuard().prepareInv(inventory, users);
    }

    private InventoryGuardFactory prepareInv(Inventory inventory, User... users)
    {
        this.currentGuardConfig = new ThreadLocal<>();
        this.currentGuardConfig.set(new InventoryGuard(this.core, inventory, users));
        return this;
    }

    /**
     * Saves the configured settings and optional open the inventory for given user
     *
     * @param openInventory
     */
    public void submitInventory(Module module, boolean openInventory)
    {
        this.currentGuardConfig.get().submitInventory(module, openInventory);
    }

    /**
     * Blocks every action that puts items into the top inventory
     *
     * @return fluent interface
     */
    public InventoryGuardFactory blockPutInAll()
    {
        this.currentGuardConfig.get().blockAll(true);
        return this;

    }

    /**
     * Blocks every action that puts any of given items into the top inventory.
     * <p>To only block an item over a certain amount use notBlockPutIn(...)
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory blockPutIn(ItemStack... items)
    {
        List<GuardedItemStack> list = new ArrayList<>();
        for (ItemStack item : items)
        {
            list.add(new GuardedItemStack(item,item.getAmount()));
        }
        this.currentGuardConfig.get().filter(true, true, list);
        return this;

    }

    /**
     * Does not block an action that puts any of given items into the top inventory.
     * <p>The amount of the ItemStack is the maximum amount that will be allowed in the inventory.
     * <p>Use 0 as amount to always allow to put in given item.
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory notBlockPutIn(ItemStack... items)
    {
        List<GuardedItemStack> list = new ArrayList<>();
        for (ItemStack item : items)
        {
            list.add(new GuardedItemStack(item,item.getAmount()));
        }
        this.currentGuardConfig.get().filter(true, false, list);
        return this;
    }

    /**
     * Blocks every action that takes items out of the top inventory
     *
     * @return fluent interface
     */
    public InventoryGuardFactory blockTakeOutAll()
    {
        this.currentGuardConfig.get().blockAll(false);
        return this;
    }

    /**
     * Blocks every action that takes any of given items out of the top inventory.
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory blockTakeOut(ItemStack... items)
    {
        List<GuardedItemStack> list = new ArrayList<>();
        for (ItemStack item : items)
        {
            list.add(new GuardedItemStack(item,item.getAmount()));
        }
        this.currentGuardConfig.get().filter(false, true, list);
        return this;
    }

    /**
     * Does not block an action that puts any of given items into the top inventory.
     * <p>The amount of the ItemStack is the minimum amount of that item that has to remain in the inventory.
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory notBlockTakeOut(ItemStack... items)
    {
        List<GuardedItemStack> list = new ArrayList<>();
        for (ItemStack item : items)
        {
            list.add(new GuardedItemStack(item,item.getAmount()));
        }
        this.currentGuardConfig.get().filter(false, false, list);
        return this;
    }

    public InventoryGuardFactory onClose(Runnable run)
    {
        this.currentGuardConfig.get().addOnClose(run);
        return this;
    }

    public InventoryGuardFactory onChange(Runnable run)
    {
        this.currentGuardConfig.get().addOnChange(run);
        return this;
    }

    public InventoryGuardFactory doNotIgnoreRepairCost()
    {
        this.currentGuardConfig.get().setIgnoreRepaircost(false);
        return this;
    }

    /**
     * This is the default behaviour
     *
     * @return
     */
    public InventoryGuardFactory ignoreRepairCost()
    {
        this.currentGuardConfig.get().setIgnoreRepaircost(true);
        return this;
    }
}
