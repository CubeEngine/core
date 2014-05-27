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


import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.messagecompositor.macro.MacroContext;

import static de.cubeisland.engine.messagecompositor.macro.AbstractFormatter.toSet;

public class StringFormatter extends ColoredFormatter<String>
{
    public StringFormatter()
    {
        super(toSet("name","input","message"));
    }

    @Override
    public String process(ChatFormat color, String object, MacroContext context)
    {
        if (color == null)
        {
            color = ChatFormat.GOLD;
        }
        return color + object;
    }
}
