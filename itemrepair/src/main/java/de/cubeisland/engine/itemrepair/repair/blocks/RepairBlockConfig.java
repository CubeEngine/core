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

import java.io.File;

import org.bukkit.Material;

import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Option;

public class RepairBlockConfig extends YamlConfiguration
{
    @Option("title")
    public String title;
    @Option("block")
    public Material blockType;
    @Option("break-percentage")
    public float breakPercentage;
    @Option("fail-percentage")
    public float failPercentage;
    @Option("cost-percentage")
    public float costPercentage;
    @Option("loose-enchantments-percentage")
    public float looseEnchPercentage;

    public static RepairBlockConfig defaultNormal()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Normal Repair";
        config.blockType = Material.IRON_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 100;
        config.looseEnchPercentage = 0;
        config.failPercentage = 0;
        return config;
    }

    public static RepairBlockConfig defaultCheap()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Cheap Repair";
        config.blockType = Material.DIAMOND_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 70;
        config.looseEnchPercentage = 5;
        config.failPercentage = 15;
        return config;
    }

    @Override
    public void onLoaded(File loadFrom)
    {
        if (!this.blockType.isBlock())
        {
            throw new InvalidConfigurationException("blockType must be a block!");
        }
    }
}
