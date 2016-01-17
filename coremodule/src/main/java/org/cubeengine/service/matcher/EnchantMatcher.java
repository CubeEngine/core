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
package org.cubeengine.service.matcher;

import java.util.HashMap;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;

import static org.spongepowered.api.data.manipulator.catalog.CatalogItemData.ENCHANTMENT_DATA;

/**
 * This Matcher provides methods to match Enchantments.
 */
@ServiceProvider(EnchantMatcher.class)
public class EnchantMatcher
{
    private final HashMap<String, Enchantment> spongeNames;
    private StringMatcher stringMatcher;

    @Inject
    public EnchantMatcher(Game game, StringMatcher stringMatcher)
    {
        this.stringMatcher = stringMatcher;
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
        String match = stringMatcher.matchString(s, spongeNames.keySet());
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
        ItemEnchantment enchantment= new ItemEnchantment(ench, enchStrength);
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
