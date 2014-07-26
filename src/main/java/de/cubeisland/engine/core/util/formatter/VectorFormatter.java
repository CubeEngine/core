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
package de.cubeisland.engine.core.util.formatter;

import de.cubeisland.engine.core.util.math.BlockVector2;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.messagecompositor.macro.MacroContext;
import de.cubeisland.engine.messagecompositor.macro.reflected.Format;
import de.cubeisland.engine.messagecompositor.macro.reflected.Names;
import de.cubeisland.engine.messagecompositor.macro.reflected.ReflectedFormatter;

import static de.cubeisland.engine.core.util.ChatFormat.DARK_AQUA;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.WHITE;

@Names("vector")
public class VectorFormatter extends ReflectedFormatter
{
    @Format
    public String format(BlockVector2 v, MacroContext context)
    {
        String arg0 = context.getArg(0);
        String arg1 = context.getArg(1);
        if (arg0 != null && arg1 != null)
        {
            return DARK_AQUA + "[" + WHITE + arg0 + GOLD + v.x +
                   DARK_AQUA + "," + WHITE + arg1 + GOLD + v.z + DARK_AQUA + "]";
        }
        return DARK_AQUA + "[" + GOLD + v.x +
               DARK_AQUA + "," + GOLD + v.z + DARK_AQUA + "]";
    }

    @Format
    public String format(BlockVector3 v, MacroContext context)
    {
        String arg0 = context.getArg(0);
        String arg1 = context.getArg(1);
        String arg2 = context.getArg(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return DARK_AQUA + "[" + WHITE + arg0 + GOLD + v.x +
                   DARK_AQUA + "," + WHITE + arg1 + GOLD + v.y + DARK_AQUA +
                   DARK_AQUA + "," + WHITE + arg2 + GOLD + v.z + DARK_AQUA + "]";
        }
        return DARK_AQUA + "[" + GOLD + v.x +
               DARK_AQUA + "," + GOLD + v.y +
               DARK_AQUA + "," + GOLD + v.z + DARK_AQUA + "]";
    }

}
