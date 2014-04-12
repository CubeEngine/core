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

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.core.util.matcher.Match;

import static org.bukkit.Material.*;

public class ContainerType
{
    public final String name;

    private ContainerType(String name)
    {
        this.name = name;
    }

    public ContainerType(InventoryHolder holder)
    {
        if (holder instanceof StorageMinecart)
        {
            this.name = STORAGE_MINECART.name();
        }
        else if (holder instanceof HopperMinecart)
        {
            this.name = Material.HOPPER_MINECART.name();
        }
        else if (holder instanceof DoubleChest)
        {
            this.name = CHEST.name();
        }
        else if (holder instanceof Chest)
        {
            this.name = CHEST.name();
        }
        else if (holder instanceof Furnace)
        {
            this.name = FURNACE.name();
        }
        else if (holder instanceof Dispenser)
        {
            this.name = DISPENSER.name();
        }
        else if (holder instanceof BrewingStand)
        {
            this.name = BREWING_STAND.name();
        }
        else if (holder instanceof Dropper)
        {
            this.name = DROPPER.name();
        }
        else if (holder instanceof Hopper)
        {
            this.name = HOPPER.name();
        }
        else
        {
            this.name = holder.getClass().getSimpleName();
        }
    }

    public static ContainerType ofName(String name)
    {
        String match = Match.string().matchString(name, STORAGE_MINECART.name(), HOPPER_MINECART.name(), CHEST.name(),
                                                  FURNACE.name(), DISPENSER.name(), DROPPER.name(), HOPPER.name(),
                                                  BREWING_STAND.name());
        if (match == null)
        {
            return new ContainerType(name);
        }
        return new ContainerType(match);
    }

    public static Material getMaterial(String material)
    {
        material = material.replace("-", "_"); // For old names saved in db
        material = material.toUpperCase(); // For old names saved in db
        return Material.matchMaterial(material);
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean equals(ContainerType other)
    {
        return name.equals(other.name);
    }

    public Material getMaterial()
    {
        return getMaterial(this.name);
    }
}

