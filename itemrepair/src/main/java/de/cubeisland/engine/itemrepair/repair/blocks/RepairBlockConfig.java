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
package de.cubeisland.engine.itemrepair.repair.blocks;

import org.bukkit.Material;

import de.cubeisland.engine.reflect.Section;

public class RepairBlockConfig implements Section
{
    public String title;
    public Material block;
    public float breakPercentage;
    public float failPercentage;
    public float costPercentage;
    public float looseEnchantmentsPercentage;

    public static RepairBlockConfig defaultNormal()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Normal Repair";
        config.block = Material.IRON_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 100;
        config.looseEnchantmentsPercentage = 0;
        config.failPercentage = 0;
        return config;
    }

    public static RepairBlockConfig defaultCheap()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Cheap Repair";
        config.block = Material.DIAMOND_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 70;
        config.looseEnchantmentsPercentage = 5;
        config.failPercentage = 15;
        return config;
    }
}
