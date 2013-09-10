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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class GuardedItemStack
{
    private  final ItemStack item;
    final int amount;

    public GuardedItemStack(ItemStack item, int amount)
    {
        this.item = item;
        this.amount = amount;
    }

    public boolean isSimilar(ItemStack other, boolean ignoreRepairCost)
    {
        if (ignoreRepairCost)
        {
            if (item.hasItemMeta() && other.hasItemMeta())
            {
                if (item.getItemMeta() instanceof Repairable && other.getItemMeta() instanceof Repairable)
                {
                    ItemMeta itemMeta = item.getItemMeta();
                    ((Repairable)itemMeta).setRepairCost(((Repairable)other.getItemMeta()).getRepairCost());
                    item.setItemMeta(itemMeta);
                }
            }
        }
        return this.item.isSimilar(other);
    }
}
