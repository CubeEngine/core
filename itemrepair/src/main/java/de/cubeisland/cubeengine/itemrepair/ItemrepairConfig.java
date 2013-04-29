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
package de.cubeisland.cubeengine.itemrepair;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.itemrepair.material.BaseMaterialContainer;
import de.cubeisland.cubeengine.itemrepair.material.BaseMaterialContainerConverter;
import de.cubeisland.cubeengine.itemrepair.repair.blocks.RepairBlockConfig;


@Codec("yml")
@DefaultConfig
@MapComments(value = @MapComment(path = "price.enchant-multiplier", text = "factor x base^EnchantmentLevel"))
public class ItemrepairConfig extends Configuration
{
    static
    {
        Convert.registerConverter(BaseMaterialContainer.class, new BaseMaterialContainerConverter());
    }

    @Option("server.bank")
    public String serverBank = "server";
    @Option("server.player")
    public String serverPlayer = "";
    @Option("price.enchant-multiplier.base")
    public float enchMultiplierBase = 1.75f;
    @Option("price.enchant-multiplier.factor")
    public float enchMultiplierFactor = 2.2f;
    @Option("repair-blocks")
    public Map<String,RepairBlockConfig> repairBlockConfigs = new HashMap<String, RepairBlockConfig>()
    {
        {
            this.put("normal",RepairBlockConfig.defaultNormal());
            this.put("cheap",RepairBlockConfig.defaultCheap());
        }
    };

    @Option("price.materials")
    public BaseMaterialContainer baseMaterials = new BaseMaterialContainer();
}
