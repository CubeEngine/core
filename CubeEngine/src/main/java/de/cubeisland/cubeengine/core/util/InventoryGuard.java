package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryGuard
{
    public InventoryGuard(Core core)
    {
        this.core = core;
    }

    private final Core core;
    private InventoryGuardConfiguration currentGuardConfig;

    public static InventoryGuard prepareInventory(Inventory inventory, User... users)
    {
        return CubeEngine.getCore().getInventoryGuard().prepareInv(inventory, users);
    }

    private InventoryGuard prepareInv(Inventory inventory, User... users)
    {
        this.currentGuardConfig = new InventoryGuardConfiguration(this.core, inventory, users);
        return this;
    }

    /**
     * Saves the configured settings and optional open the inventory for given user
     *
     * @param openInventory
     */
    public void submitInventory(Module module, boolean openInventory)
    {
        this.currentGuardConfig.submitInventory(module, openInventory);
    }

    /**
     * Blocks every action that puts items into the top inventory
     *
     * @return fluent interface
     */
    public InventoryGuard blockPutInAll()
    {
        this.currentGuardConfig.blockAll(true);
        return this;

    }

    /**
     * Blocks every action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuard blockPutIn(ItemStack... items)
    {
        this.currentGuardConfig.filter(true, true, items);
        return this;

    }

    /**
     * Does not block an action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuard notBlockPutIn(ItemStack... items)
    {
        this.currentGuardConfig.filter(true, false, items);
        return this;
    }

    /**
     * Blocks every action that takes items out of the top inventory
     *
     * @return fluent interface
     */
    public InventoryGuard blockTakeOutAll()
    {
        this.currentGuardConfig.blockAll(false);
        return this;
    }

    /**
     * Blocks every action that takes any of given items out of the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuard blockTakeOut(ItemStack... items)
    {
        this.currentGuardConfig.filter(false, true, items);
        return this;
    }

    /**
     * Does not block an action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuard notBlockTakeOut(ItemStack... items)
    {
        this.currentGuardConfig.filter(false, false, items);
        return this;
    }

    public InventoryGuard onClose(Runnable run)
    {
        this.currentGuardConfig.addOnClose(run);
        return this;
    }

    public InventoryGuard onChange(Runnable run)
    {
        this.currentGuardConfig.addOnChange(run);
        return this;
    }
}
