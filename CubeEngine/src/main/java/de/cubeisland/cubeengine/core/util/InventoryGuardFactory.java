package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        this.currentGuardConfig = new ThreadLocal<InventoryGuard>();
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
     * Blocks every action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory blockPutIn(ItemStack... items)
    {
        this.currentGuardConfig.get().filter(true, true, items);
        return this;

    }

    /**
     * Does not block an action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory notBlockPutIn(ItemStack... items)
    {
        this.currentGuardConfig.get().filter(true, false, items);
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
     * Blocks every action that takes any of given items out of the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory blockTakeOut(ItemStack... items)
    {
        this.currentGuardConfig.get().filter(false, true, items);
        return this;
    }

    /**
     * Does not block an action that puts any of given items into the top inventory
     *
     * @param items
     * @return fluent interface
     */
    public InventoryGuardFactory notBlockTakeOut(ItemStack... items)
    {
        this.currentGuardConfig.get().filter(false, false, items);
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
}
