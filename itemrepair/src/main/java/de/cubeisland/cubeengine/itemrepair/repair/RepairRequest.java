package de.cubeisland.cubeengine.itemrepair.repair;

import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.itemrepair.repair.blocks.RepairBlock;

public class RepairRequest
{
    private final RepairBlock repairBlock;
    private final Inventory inventory;
    private final Map<Integer, ItemStack> items;
    private final double price;

    public RepairRequest(RepairBlock repairBlock, Inventory inventory, Map<Integer, ItemStack> items, double price)
    {
        if (repairBlock == null)
        {
            throw new IllegalArgumentException("repairBlock must not be null!");
        }
        this.repairBlock = repairBlock;
        this.inventory = inventory;
        this.items = items;
        this.price = price;
    }

    public RepairBlock getRepairBlock()
    {
        return this.repairBlock;
    }

    public Inventory getInventory()
    {
        return this.inventory;
    }

    public Map<Integer, ItemStack> getItems()
    {
        return this.items;
    }

    public double getPrice()
    {
        return this.price;
    }
}
