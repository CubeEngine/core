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
package de.cubeisland.engine.multiverse.player;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.config.codec.NBTCodec;

public class PlayerConfiguration extends Configuration<NBTCodec>
{
    public int selectedItemSlot;
    public double health = 20;
    public double maxHealth = 20;
    public int foodLevel = 20;
    public float saturation = 20;
    public float exhaustion = 0;
    public int expTotal = 0;
    public int fireTicks = 0;

    public Collection<PotionEffect> activePotionEffects;
    public Inventory inventory;
    public Inventory enderChest;

    public void applyToPlayer(Player player)
    {
        Inventory inv = player.getInventory();
        player.getInventory().setHeldItemSlot(selectedItemSlot);
        player.setMaxHealth(maxHealth);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
        player.setTotalExperience(expTotal);
        player.setFireTicks(fireTicks);
        player.addPotionEffects(activePotionEffects);

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++)
        {
            if (i >= inv.getSize() + 4)
            {
                break;
            }
            inv.setItem(i, contents[i]);
        }

        inv = player.getEnderChest();
        contents = enderChest.getContents();
        for (int i = 0; i < contents.length; i++)
        {
            if (i >= inv.getSize())
            {
                break;
            }
            inv.setItem(i, contents[i]);
        }
    }
}
