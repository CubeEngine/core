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

import org.bukkit.ChatColor;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.math.BlockVector2;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.messagecompositor.macro.MacroContext;
import de.cubeisland.engine.messagecompositor.macro.reflected.Format;
import de.cubeisland.engine.messagecompositor.macro.reflected.Names;
import de.cubeisland.engine.messagecompositor.macro.reflected.ReflectedFormatter;

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
            return ChatFormat.DARK_AQUA + "[" + ChatColor.WHITE + arg0 + ChatColor.GOLD + v.x +
                   ChatFormat.DARK_AQUA + "," + ChatColor.WHITE + arg1 + ChatColor.GOLD + v.z + ChatFormat.DARK_AQUA + "]";
        }
        return ChatFormat.DARK_AQUA + "[" + ChatColor.GOLD + v.x +
               ChatFormat.DARK_AQUA + "," + ChatColor.GOLD + v.z + ChatFormat.DARK_AQUA + "]";
    }

    @Format
    public String format(BlockVector3 v, MacroContext context)
    {
        String arg0 = context.getArg(0);
        String arg1 = context.getArg(1);
        String arg2 = context.getArg(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return ChatFormat.DARK_AQUA + "[" + ChatColor.WHITE + arg0 + ChatColor.GOLD + v.x +
                   ChatFormat.DARK_AQUA + "," + ChatColor.WHITE + arg1 + ChatColor.GOLD + v.y + ChatFormat.DARK_AQUA +
                   ChatFormat.DARK_AQUA + "," + ChatColor.WHITE + arg2 + ChatColor.GOLD + v.z + ChatFormat.DARK_AQUA + "]";
        }
        return ChatFormat.DARK_AQUA + "[" + ChatColor.GOLD + v.x +
               ChatFormat.DARK_AQUA + "," + ChatColor.GOLD + v.y +
               ChatFormat.DARK_AQUA + "," + ChatColor.GOLD + v.z + ChatFormat.DARK_AQUA + "]";
    }

}
