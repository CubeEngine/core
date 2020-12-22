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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This Matcher provides methods to match Enchantments.
 */
public class EnchantMatcher
{
    private final HashMap<String, EnchantmentType> names = new HashMap<>();
    private final HashMap<String, EnchantmentType> ids = new HashMap<>();
    private StringMatcher sm;

    @Inject
    public EnchantMatcher(StringMatcher stringMatcher)
    {
        this.sm = stringMatcher;
        Sponge.getGame().registries().registry(RegistryTypes.ENCHANTMENT_TYPE).streamEntries().forEach(entry -> {
            this.ids.put(entry.key().getFormatted(), entry.value());
            if ("minecraft".equals(entry.key().getNamespace()))
            {
                this.ids.put(entry.key().getValue(), entry.value());
            }
// TODO            this.names.put(enchantment.getTranslation().get(), enchantment);
        });
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
            final List<Enchantment> data = item.getOrElse(Keys.APPLIED_ENCHANTMENTS, new ArrayList<>());
            data.add(enchantment);
            item.offer(Keys.APPLIED_ENCHANTMENTS, data);
            return true;
        }
        try
        {
            // TODO check if enchantment is allowed
            final List<Enchantment> data = item.getOrElse(Keys.APPLIED_ENCHANTMENTS, new ArrayList<>());
            data.add(enchantment);
            item.offer(Keys.APPLIED_ENCHANTMENTS, data);
            return true;
        }
        catch (IllegalArgumentException ignored)
        {
            return false;
        }
    }
}
