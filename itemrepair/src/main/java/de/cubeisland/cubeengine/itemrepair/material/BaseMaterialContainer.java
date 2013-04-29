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
package de.cubeisland.cubeengine.itemrepair.material;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;

public class BaseMaterialContainer
{
    private Map<Material,BaseMaterial> baseMaterials = new HashMap<Material, BaseMaterial>();

    public BaseMaterialContainer()
    {
        this.registerBaseMaterial(new BaseMaterial(Material.WOOD, 0.2));
        this.registerBaseMaterial(new BaseMaterial(Material.STONE, 0.1));
        this.registerBaseMaterial(new BaseMaterial(Material.IRON_INGOT, 0.1));
        this.registerBaseMaterial(new BaseMaterial(Material.GOLD_INGOT, 0.1));
        this.registerBaseMaterial(new BaseMaterial(Material.DIAMOND, 0.1));
        this.registerBaseMaterial(new BaseMaterial(Material.LEATHER, 0.1));
        this.registerBaseMaterial(new BaseMaterial(Material.FIRE, 0.1));
    }

    public BaseMaterialContainer(Map<Material, Double> map)
    {
        for (Entry<Material, Double> entry : map.entrySet())
        {
            this.registerBaseMaterial(new BaseMaterial(entry.getKey(),entry.getValue()));
        }
    }

    public void registerBaseMaterial(BaseMaterial baseMaterial)
    {
        this.baseMaterials.put(baseMaterial.getMaterial(),baseMaterial);
    }

    public BaseMaterial of(Material mat)
    {
        return this.baseMaterials.get(mat);
    }

    public Map<Material, BaseMaterial> getBaseMaterials()
    {
        return baseMaterials;
    }
}
