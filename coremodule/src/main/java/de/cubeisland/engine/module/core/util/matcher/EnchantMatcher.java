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
package de.cubeisland.engine.module.core.util.matcher;

import java.util.HashMap;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.manipulators.items.EnchantmentData;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * This Matcher provides methods to match Enchantments.
 */
public class EnchantMatcher
{
    private final HashMap<String, Enchantment> spongeNames;
    private CoreModule core;

    EnchantMatcher(CoreModule core, Game game)
    {
        this.core = core;
        this.spongeNames = new HashMap<>();
        for (Enchantment enchantment : game.getRegistry().getAllOf(Enchantment.class))
        {
            this.spongeNames.put(enchantment.getName(), enchantment);
        }
    }

    /**
     * Tries to match an Enchantment for given string
     *
     * @param s the string to match
     * @return the found Enchantment
     */
    public Enchantment enchantment(String s)
    {
        String match = core.getModularity().start(StringMatcher.class).matchString(s, spongeNames.keySet());
        return spongeNames.get(match);
    }

    public boolean applyMatchedEnchantment(ItemStack item, String enchName, int enchStrength, boolean force)
    {
        Enchantment ench = this.enchantment(enchName);
        if (ench == null)
            return false;
        if (enchStrength == 0)
        {
            enchStrength = ench.getMaximumLevel();
        }
        if (force)
        {
            EnchantmentData data = item.getOrCreate(EnchantmentData.class).get();
            data.setUnsafe(ench, enchStrength);
            return true;
        }
        try
        {
            EnchantmentData data = item.getOrCreate(EnchantmentData.class).get();
            data.set(ench, enchStrength);
            return true;
        }
        catch (IllegalArgumentException ignored)
        {
            return false;
        }
    }
}
