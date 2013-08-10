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
package de.cubeisland.engine.itemrepair.material;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.*;

public class RepairItemContainer
{
    private final Map<Material, RepairItem> repairItems = new EnumMap<>(Material.class);
    private final BaseMaterialContainer baseMat;

    public RepairItemContainer(BaseMaterialContainer baseMat)
    {
        this.baseMat = baseMat;
        this.registerDefaultRepairItems();
    }

    private void registerDefaultRepairItems()
    {
        // TOOLS
        // IRON
        this.registerRepairItem(RepairItem.of(IRON_SPADE,baseMat.of(IRON_INGOT),1))
            .registerRepairItem(RepairItem.of(IRON_PICKAXE,baseMat.of(IRON_INGOT),3))
            .registerRepairItem(RepairItem.of(IRON_AXE,baseMat.of(IRON_INGOT),3))
            .registerRepairItem(RepairItem.of(FLINT_AND_STEEL,baseMat.of(IRON_INGOT),1))
            .registerRepairItem(RepairItem.of(IRON_SWORD,baseMat.of(IRON_INGOT),2))
            .registerRepairItem(RepairItem.of(IRON_HOE,baseMat.of(IRON_INGOT),2))
            .registerRepairItem(RepairItem.of(SHEARS,baseMat.of(IRON_INGOT),2))
        // WOOD
            .registerRepairItem(RepairItem.of(BOW, baseMat.of(WOOD), 2))
            .registerRepairItem(RepairItem.of(WOOD_SWORD, baseMat.of(WOOD), 2))
            .registerRepairItem(RepairItem.of(WOOD_SPADE, baseMat.of(WOOD), 1))
            .registerRepairItem(RepairItem.of(WOOD_PICKAXE, baseMat.of(WOOD), 3))
            .registerRepairItem(RepairItem.of(WOOD_AXE, baseMat.of(WOOD), 3))
            .registerRepairItem(RepairItem.of(WOOD_HOE, baseMat.of(WOOD), 2))
            .registerRepairItem(RepairItem.of(FISHING_ROD, baseMat.of(WOOD), 2))
        // STONE
            .registerRepairItem(RepairItem.of(STONE_SWORD, baseMat.of(STONE), 2))
            .registerRepairItem(RepairItem.of(STONE_SPADE, baseMat.of(STONE), 1))
            .registerRepairItem(RepairItem.of(STONE_PICKAXE, baseMat.of(STONE), 3))
            .registerRepairItem(RepairItem.of(STONE_AXE, baseMat.of(STONE), 3))
        // DIAMOND
            .registerRepairItem(RepairItem.of(DIAMOND_SWORD, baseMat.of(DIAMOND), 2))
            .registerRepairItem(RepairItem.of(DIAMOND_SPADE, baseMat.of(DIAMOND), 1))
            .registerRepairItem(RepairItem.of(DIAMOND_PICKAXE, baseMat.of(DIAMOND), 3))
            .registerRepairItem(RepairItem.of(DIAMOND_AXE, baseMat.of(DIAMOND), 3))
            .registerRepairItem(RepairItem.of(DIAMOND_HOE, baseMat.of(DIAMOND), 2))
        // GOLD
            .registerRepairItem(RepairItem.of(GOLD_SWORD, baseMat.of(GOLD_INGOT), 2))
            .registerRepairItem(RepairItem.of(GOLD_SPADE, baseMat.of(GOLD_INGOT), 1))
            .registerRepairItem(RepairItem.of(GOLD_PICKAXE, baseMat.of(GOLD_INGOT), 3))
            .registerRepairItem(RepairItem.of(GOLD_AXE, baseMat.of(GOLD_INGOT), 3))
            .registerRepairItem(RepairItem.of(GOLD_HOE, baseMat.of(GOLD_INGOT), 2))
        // ARMOR
        // LEATHER
            .registerRepairItem(RepairItem.of(LEATHER_HELMET, baseMat.of(LEATHER), 5))
            .registerRepairItem(RepairItem.of(LEATHER_CHESTPLATE, baseMat.of(LEATHER), 8))
            .registerRepairItem(RepairItem.of(LEATHER_LEGGINGS, baseMat.of(LEATHER), 7))
            .registerRepairItem(RepairItem.of(LEATHER_BOOTS, baseMat.of(LEATHER), 4))
        // CHAINMAIL
            .registerRepairItem(RepairItem.of(CHAINMAIL_HELMET, baseMat.of(FIRE), 5))
            .registerRepairItem(RepairItem.of(CHAINMAIL_CHESTPLATE, baseMat.of(FIRE), 8))
            .registerRepairItem(RepairItem.of(CHAINMAIL_LEGGINGS, baseMat.of(FIRE), 7))
            .registerRepairItem(RepairItem.of(CHAINMAIL_BOOTS, baseMat.of(FIRE), 4))
        // IRON
            .registerRepairItem(RepairItem.of(IRON_HELMET, baseMat.of(IRON_INGOT), 5))
            .registerRepairItem(RepairItem.of(IRON_CHESTPLATE, baseMat.of(IRON_INGOT), 8))
            .registerRepairItem(RepairItem.of(IRON_LEGGINGS, baseMat.of(IRON_INGOT), 7))
            .registerRepairItem(RepairItem.of(IRON_BOOTS, baseMat.of(IRON_INGOT), 4))
        // DIAMOND
            .registerRepairItem(RepairItem.of(DIAMOND_HELMET, baseMat.of(DIAMOND), 5))
            .registerRepairItem(RepairItem.of(DIAMOND_CHESTPLATE, baseMat.of(DIAMOND), 8))
            .registerRepairItem(RepairItem.of(DIAMOND_LEGGINGS, baseMat.of(DIAMOND), 7))
            .registerRepairItem(RepairItem.of(DIAMOND_BOOTS, baseMat.of(DIAMOND), 4))
        // GOLD
            .registerRepairItem(RepairItem.of(GOLD_HELMET, baseMat.of(GOLD_INGOT), 5))
            .registerRepairItem(RepairItem.of(GOLD_CHESTPLATE, baseMat.of(GOLD_INGOT), 8))
            .registerRepairItem(RepairItem.of(GOLD_LEGGINGS, baseMat.of(GOLD_INGOT), 7))
            .registerRepairItem(RepairItem.of(GOLD_BOOTS, baseMat.of(GOLD_INGOT), 4));
    }

    public Map<Integer, ItemStack> getRepairableItems(Inventory inventory)
    {
        Map<Integer, ItemStack> items = new HashMap<>();

        ItemStack item;
        for (int i = 0; i < inventory.getSize(); ++i)
        {
            item = inventory.getItem(i);
            if (item != null && this.of(item.getType()) != null && item.getDurability() > 0)
            {
                items.put(i, item);
            }
        }

        return items;
    }

    public RepairItemContainer registerRepairItem(RepairItem repairItem)
    {
        if (repairItem == null) return this;
        this.repairItems.put(repairItem.getMaterial(),repairItem);
        return this;
    }

    public BaseMaterialContainer getPriceProvider()
    {
        return this.baseMat;
    }

    public RepairItem of(Material mat)
    {
        return this.repairItems.get(mat);
    }
}
