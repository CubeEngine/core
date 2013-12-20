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
package de.cubeisland.engine.worlds.player;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.config.codec.NBTCodec;

public class PlayerDataConfig extends Configuration<NBTCodec>
{
    public int heldItemSlot = 0;
    public double health = 20;
    public double maxHealth = 20;
    public int foodLevel = 20;
    public float saturation = 20;
    public float exhaustion = 0;
    public float exp = 0;
    public int lvl = 0;
    public int fireTicks = 0;

    public Collection<PotionEffect> activePotionEffects = new ArrayList<>();
    public Inventory inventory;
    public Inventory enderChest;

    private transient String[] head = null;

    public void applyToPlayer(Player player)
    {
        Inventory inv = player.getInventory();
        player.getInventory().setHeldItemSlot(heldItemSlot);
        player.setMaxHealth(maxHealth);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);
        player.setLevel(lvl);
        player.setExp(exp);
        player.setFireTicks(fireTicks);

        for (PotionEffect potionEffect : player.getActivePotionEffects())
        {
            player.removePotionEffect(potionEffect.getType());
        }
        player.addPotionEffects(activePotionEffects);

        ItemStack[] contents;
        if (inventory == null)
        {
            contents = new ItemStack[36+4];
        }
        else
        {
            contents = inventory.getContents();
        }

        for (int i = 0; i < contents.length; i++)
        {
            if (i >= inv.getSize() + 4)
            {
                break;
            }
            inv.setItem(i, contents[i]);
        }

        inv = player.getEnderChest();
        if (inventory == null)
        {
            contents = new ItemStack[27];
        }
        else
        {
            contents = enderChest.getContents();
        }
        for (int i = 0; i < contents.length; i++)
        {
            if (i >= inv.getSize())
            {
                break;
            }
            inv.setItem(i, contents[i]);
        }
    }

    public void applyFromPlayer(Player player)
    {
        this.heldItemSlot = player.getInventory().getHeldItemSlot();
        this.maxHealth = player.getMaxHealth();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();
        this.exhaustion = player.getExhaustion();
        this.lvl = player.getLevel();
        this.exp = player.getExp();
        this.fireTicks = player.getFireTicks();
        this.activePotionEffects = player.getActivePotionEffects();
        this.inventory = player.getInventory();
        this.enderChest = player.getEnderChest();
    }

    public void setHead(String... head)
    {
        this.head = head;
    }

    @Override
    public String[] head()
    {
        return this.head;
    }
}
