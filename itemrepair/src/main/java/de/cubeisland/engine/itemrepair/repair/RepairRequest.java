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
package de.cubeisland.engine.itemrepair.repair;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock.RepairBlockInventory;

public class RepairRequest
{
    private final RepairBlock repairBlock;
    private final RepairBlockInventory inventory;
    private final Map<Integer, ItemStack> items;
    private final double price;

    public RepairRequest(RepairBlock repairBlock, RepairBlockInventory inventory, Map<Integer, ItemStack> items, double price)
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

    public RepairBlockInventory getInventory()
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
