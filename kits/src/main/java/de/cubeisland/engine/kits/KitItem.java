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
package de.cubeisland.engine.kits;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitItem
{
    public Material mat;
    public short dura;
    public int amount;
    public String customName;
    public Map<Enchantment, Integer> enchs;
    private ItemStack item;

    public KitItem(Material mat, short dura, int amount, String customName, Map<Enchantment, Integer> enchs)
    {
        this.mat = mat;
        this.dura = dura;
        this.amount = amount;
        this.customName = customName;
        this.enchs = enchs;
    }

    public ItemStack getItemStack()
    {
        if (item != null) return item;
        item = new ItemStack(mat, amount, dura);
        if (customName != null)
        {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(customName);
            item.setItemMeta(meta);
        }
        if (enchs != null && !enchs.isEmpty())
        {
            item.addUnsafeEnchantments(enchs);
        }
        return item;
    }
}
