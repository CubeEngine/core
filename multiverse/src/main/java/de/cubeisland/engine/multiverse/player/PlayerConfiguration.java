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

import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.config.codec.NBTCodec;

public class PlayerConfiguration extends Configuration<NBTCodec>
{
    public int selectedItemSlot;
    public double health;
    public double maxHealth;
    public float saturation;
    public int foodLevel;
    public float expTotal;
    public int fireTicks;

    public Collection<PotionEffect> activePotionEffects;
    public Inventory inventory;
    public Inventory enderChest;
    // count slot damage id +moar nbt
}
