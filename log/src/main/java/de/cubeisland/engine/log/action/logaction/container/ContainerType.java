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
package de.cubeisland.engine.log.action.logaction.container;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.core.util.matcher.Match;

public class ContainerType
{
    public final String name;

    private ContainerType(String name)
    {
        this.name = name;
    }

    public static ContainerType ofName(String name)
    {
        String match = Match.string().matchString(name, 
              Material.STORAGE_MINECART.name(), 
              Material.CHEST.name(),
              Material.FURNACE.name(),
              Material.DISPENSER.name(),
              Material.DROPPER.name(),
              Material.HOPPER.name(),
              Material.BREWING_STAND.name()
              );// TODO HopperMinecart??
        if (match == null)
        {
            return null;
        }
        return new ContainerType(match);
    }

    public ContainerType (InventoryHolder holder)
    {
        if (holder instanceof StorageMinecart)
        {
            this.name = Material.STORAGE_MINECART.name();
        }
        else if (holder instanceof DoubleChest)
        {
            this.name = Material.CHEST.name();
        }
        else if (holder instanceof Chest)
        {
            this.name = Material.CHEST.name();
        }
        else if (holder instanceof Furnace)
        {
            this.name = Material.FURNACE.name();
        }
        else if (holder instanceof Dispenser)
        {
            this.name = Material.DISPENSER.name();
        }
        else if (holder instanceof BrewingStand)
        {
            this.name = Material.BREWING_STAND.name();
        }
        else if (holder instanceof Dropper)
        {
            this.name = Material.DROPPER.name();
        }
        else if (holder instanceof Hopper)
        {
            this.name = Material.HOPPER.name();
        }
        else
        {
            this.name = null;
        }
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static Material getMaterial(String material)
    {
        material = material.replace("-", "_"); // For old names saved in db
        material = material.toUpperCase(); // For old names saved in db
        return Material.matchMaterial(material);
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

        ContainerType that = (ContainerType)o;

        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }

    public Material getMaterial()
    {
        return getMaterial(this.name);
    }
}

