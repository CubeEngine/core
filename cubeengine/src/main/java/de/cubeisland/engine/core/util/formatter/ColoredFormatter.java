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

import java.util.Set;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.messagecompositor.macro.AbstractFormatter;
import de.cubeisland.engine.messagecompositor.macro.MacroContext;
import de.cubeisland.engine.messagecompositor.macro.Reader;

public abstract class ColoredFormatter<T> extends AbstractFormatter<T>
{
    protected ColoredFormatter(Set<String> names)
    {
        super(names);
    }

    @Override
    public String process(T object, MacroContext context)
    {
        return this.process(context.readMapped("color", ChatFormat.class), object, context);
    }

    public abstract String process(ChatFormat color, T object, MacroContext context);

    public static class ColorReader implements Reader<ChatFormat>
    {
        @Override
        public ChatFormat read(String raw)
        {
            return ChatFormat.valueOf(raw.toUpperCase());
        }
    }
}
