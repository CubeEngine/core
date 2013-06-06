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
package de.cubeisland.cubeengine.log.storage;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class ImmutableBlockData
{
    public final Material material;
    public final Byte data;

    public ImmutableBlockData(BlockState blockState)
    {
        this(blockState.getType(), blockState.getRawData());
    }

    public ImmutableBlockData(Material material)
    {
        this(material,null);
    }

    public ImmutableBlockData(Material material, Byte data)
    {
        this.material = material;
        this.data = data;
    }

    @Override
    public String toString()
    {
        //Match.material() //TODO getPrettyName
        return material + ":" + data;
    }
}
