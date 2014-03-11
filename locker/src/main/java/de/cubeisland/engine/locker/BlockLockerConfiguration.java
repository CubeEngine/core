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
package de.cubeisland.engine.locker;

import org.bukkit.Material;

import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.locker.storage.ProtectedType;

/**
 * Example:
 * B_DOOR:
 *   auto-protect: PRIVATE
 *   flags:
 *      - BLOCK_REDSTONE
 *      - AUTOCLOSE
 */
public class BlockLockerConfiguration extends LockerSubConfig<BlockLockerConfiguration, Material>
{
    public BlockLockerConfiguration(Material material)
    {
        super(ProtectedType.getProtectedType(material));
        this.type = material;
    }

    public String getTitle()
    {
        return type.name();
    }

    public static class BlockLockerConfigConverter extends LockerSubConfigConverter<BlockLockerConfiguration>
    {
        protected BlockLockerConfiguration fromString(String s) throws ConversionException
        {
            Material material;
            try
            {
                material = Material.valueOf(s);
            }
            catch (IllegalArgumentException ignore)
            {
                try
                {
                    material = Material.getMaterial(Integer.valueOf(s));
                }
                catch (NumberFormatException ignoreToo)
                {
                    material = Match.material().material(s);
                }
            }
            if (material == null)
            {
                throw ConversionException.of(this, s, "Invalid BlockType!");
            }
            return new BlockLockerConfiguration(material);
        }
    }
}
