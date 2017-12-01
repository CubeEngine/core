/*
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
package org.cubeengine.libcube.service.matcher;

import static org.spongepowered.api.data.manipulator.catalog.CatalogItemData.ENCHANTMENT_DATA;

import org.cubeengine.libcube.service.config.EnchantmentConverter;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;

import javax.inject.Inject;

/**
 * This Matcher provides methods to match Enchantments.
 */
public class EnchantMatcher
{
    private final HashMap<String, EnchantmentType> names = new HashMap<>();
    private final HashMap<String, EnchantmentType> ids = new HashMap<>();
    private StringMatcher sm;

    @Inject
    public EnchantMatcher(Reflector reflector, StringMatcher stringMatcher)
    {
        reflector.getDefaultConverterManager().registerConverter(new EnchantmentConverter(this), Enchantment.class);

        this.sm = stringMatcher;
        for (EnchantmentType enchantment : Sponge.getRegistry().getAllOf(EnchantmentType.class))
        {
            this.ids.put(enchantment.getName(), enchantment);
            if (enchantment.getName().startsWith("minecraft:"))
            {
                this.ids.put(enchantment.getName().substring(9), enchantment);
            }
            this.names.put(enchantment.getTranslation().get(), enchantment);
        }

    }

    /**
     * Tries to match an Enchantment for given string
     *
     * @param s the string to match
     * @return the found Enchantment
     */
    public EnchantmentType enchantment(String s)
    {
        String match = sm.matchString(s, names.keySet());
        if (match != null)
        {
            return names.get(match);
        }
        return ids.get(sm.matchString(s, ids.keySet()));
    }

    public boolean applyMatchedEnchantment(ItemStack item, String enchName, int enchStrength, boolean force)
    {
        EnchantmentType ench = this.enchantment(enchName);
        if (ench == null)
            return false;
        if (enchStrength == 0)
        {
            enchStrength = ench.getMaximumLevel();
        }
        Enchantment enchantment= Enchantment.builder().type(ench).level(enchStrength).build();
        if (force)
        {
            EnchantmentData data = item.getOrCreate(ENCHANTMENT_DATA).get();
            data.enchantments().add(enchantment);
            item.offer(data);
            return true;
        }
        try
        {
            EnchantmentData data = item.getOrCreate(EnchantmentData.class).get();
            data.enchantments().add(enchantment);
            item.offer(data);
            return true;
        }
        catch (IllegalArgumentException ignored)
        {
            return false;
        }
    }
}
