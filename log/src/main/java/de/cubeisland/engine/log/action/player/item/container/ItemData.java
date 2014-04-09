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
package de.cubeisland.engine.log.action.player.item.container;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemData
{
    public Material material;
    public short dura;
    public String displayName;
    public List<String> lore;
    public Map<Enchantment, Integer> enchantments;

    public ItemData(Material material, short dura, String displayName, List<String> lore,
                    Map<Enchantment, Integer> enchantments)
    {
        this.material = material;
        this.dura = dura;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
    }

    public ItemData(ItemStack itemStack)
    {
        this.material = itemStack.getType();
        this.dura = itemStack.getDurability();
        if (itemStack.hasItemMeta())
        {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName())
            {
                displayName = meta.getDisplayName();
            }
            if (meta.hasLore())
            {
                lore = meta.getLore();
            }
            if (meta.hasEnchants())
            {
                enchantments = meta.getEnchants();
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ItemData itemData = (ItemData)o;
        if (dura != itemData.dura)
        {
            return false;
        }
        if (displayName != null ? !displayName.equals(itemData.displayName) : itemData.displayName != null)
        {
            return false;
        }
        if (enchantments != null ? !enchantments.equals(itemData.enchantments) : itemData.enchantments != null)
        {
            return false;
        }
        if (lore != null ? !lore.equals(itemData.lore) : itemData.lore != null)
        {
            return false;
        }
        if (material != itemData.material)
        {
            return false;
        }
        // ignore amount
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = material != null ? material.hashCode() : 0;
        result = 31 * result + (int)dura;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (lore != null ? lore.hashCode() : 0);
        result = 31 * result + (enchantments != null ? enchantments.hashCode() : 0);
        return result;
    }

    public ItemStack toItemStack()
    {
        ItemStack itemStack = new ItemStack(material, 1, dura);
        ItemMeta meta = itemStack.getItemMeta();
        if (displayName != null)
        {
            meta.setDisplayName(displayName);
        }
        if (lore != null)
        {
            meta.setLore(lore);
        }
        if (enchantments != null)
        {
            meta.getEnchants().clear();
            meta.getEnchants().putAll(enchantments);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public String toString()
    {
        String result = this.material.name() + ":" + this.dura;
        if (this.displayName != null)
        {
            result += " (" + this.displayName + ")";
        }
        return result;
    }
}
