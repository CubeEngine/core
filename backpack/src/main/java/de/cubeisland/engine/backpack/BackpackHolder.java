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
package de.cubeisland.engine.backpack;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BackpackHolder implements InventoryHolder
{
    public final int index;
    public final Inventory inventory;
    private final BackpackInventories backBackInventories;

    public BackpackHolder(BackpackInventories backBackInventories, int index, int size, String title)
    {
        this.backBackInventories = backBackInventories;
        this.index = index;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory()
    {
        return this.inventory;
    }

    public BackpackInventories getBackpack()
    {
        return this.backBackInventories;
    }
}