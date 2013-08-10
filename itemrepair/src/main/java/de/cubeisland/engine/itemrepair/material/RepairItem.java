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
package de.cubeisland.engine.itemrepair.material;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class RepairItem
{
    private final Material material;
    private final Map<BaseMaterial,Integer> baseMaterials;

    private RepairItem(Material material, BaseMaterial baseMaterial, int baseMaterialCount)
    {
        this.material = material;
        this.baseMaterials = new HashMap<>();
        this.baseMaterials.put(baseMaterial,baseMaterialCount);
    }

    public RepairItem(Material material, Map<BaseMaterial,Integer> baseMaterials)
    {
        this.material = material;
        this.baseMaterials = new HashMap<>(baseMaterials);
    }

    public static RepairItem of(Material material, BaseMaterial baseMaterial, int baseMaterialCount)
    {
        if (material == null || baseMaterial == null)
        {
            return null;
        }
        return new RepairItem(material,baseMaterial,baseMaterialCount);
    }

    /**
     * Returns the material of this item
     *
     * @return the material
     */
    public Material getMaterial()
    {
        return this.material;
    }

    public Map<BaseMaterial, Integer> getBaseMaterials()
    {
        return baseMaterials;
    }
}
