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
package de.cubeisland.engine.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.AliasMapFormat;

/**
 * This Matcher provides methods to match Enchantments.
 */
public class EnchantMatcher
{
    private final HashMap<String, Enchantment> enchantments;
    private final HashMap<String, Enchantment> bukkitnames;
    private final HashMap<Enchantment, String> enchantmentName;

    EnchantMatcher()
    {
        this.bukkitnames = new HashMap<>();
        for (Enchantment enchantment : Enchantment.values())
        {
            this.bukkitnames.put(enchantment.getName(), enchantment);
        }

        this.enchantments = new HashMap<>();
        this.enchantmentName = new HashMap<>();

        TreeMap<String, List<String>> enchs = this.readEnchantments();
        for (String bukkitName : enchs.keySet())
        {
            Enchantment ench = this.bukkitnames.get(bukkitName);
            if (ench == null)
            {
                CubeEngine.getLog().warn("Unkown Enchantment: {}", bukkitName);
                continue;
            }
            this.registerEnchantment(ench, enchs.get(bukkitName));
        }
    }

    /**
     * Registers an enchantment for the matcher with a list of names
     *
     * @param ench  the enchantment
     * @param names the corresponding names
     */
    private void registerEnchantment(Enchantment ench, List<String> names)
    {
        boolean first = true;
        for (String name : names)
        {
            if (first)
            {
                this.enchantmentName.put(ench, name);
                first = false;
            }
            this.enchantments.put(name.toLowerCase(Locale.ENGLISH), ench);
        }
    }

    /**
     * Loads in the file with the saved enchantment-names
     *
     * @return the loaded enchantments with corresponding names
     */
    private TreeMap<String, List<String>> readEnchantments()
    {
        try
        {
            Path file = CubeEngine.getFileManager().getDataPath().resolve(CoreResource.ENCHANTMENTS.getTarget());
            TreeMap<String, List<String>> enchantments = new TreeMap<>();
            AliasMapFormat.parseStringList(file, enchantments, false);
            try (InputStream stream = CubeEngine.getFileManager().getSourceOf(file))
            {
                if (AliasMapFormat.parseStringList(stream, enchantments, true))
                {
                    CubeEngine.getLog().info("Updated enchantments.txt");
                    AliasMapFormat.parseAndSaveStringListMap(enchantments, file);
                }
                return enchantments;
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading enchantments.txt", ex);
        }
    }

    /**
     * Gets the name for the given Enchantment
     *
     * @param enchant the enchantment to get the name for
     * @return the name corresponding to the enchantment
     */
    public String nameFor(Enchantment enchant)
    {
        return this.enchantmentName.get(enchant);
    }

    /**
     * Tries to match an Enchantment for given string
     *
     * @param s the string to match
     * @return the found Enchantment
     */
    public Enchantment enchantment(String s)
    {
        Enchantment enchantment = this.enchantments.get(s.toLowerCase(Locale.ENGLISH));
        try
        {
            int enchId = Integer.parseInt(s);
            return Enchantment.getById(enchId);
        }
        catch (NumberFormatException ignored)
        {}
        if (enchantment == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            String t_key = Match.string().matchString(s, this.enchantments.keySet());
            if (t_key != null)
            {
                return this.enchantments.get(t_key);
            }
            else
            {
                t_key = Match.string().matchString(s, this.bukkitnames.keySet());
                if (t_key != null)
                {
                    return this.bukkitnames.get(t_key);
                }
            }
        }
        return enchantment;
    }

    public boolean applyMatchedEnchantment(ItemStack item, String enchName, int enchStrength, boolean force)
    {
        Enchantment ench = this.enchantment(enchName);
        if (ench == null)
            return false;
        if (enchStrength == 0)
        {
            enchStrength = ench.getMaxLevel();
        }
        if (force)
        {
            item.addUnsafeEnchantment(ench, enchStrength);
            return true;
        }
        try
        {
            item.addEnchantment(ench, enchStrength);
            return true;
        }
        catch (IllegalArgumentException ignored)
        {
            return false;
        }
    }
}
