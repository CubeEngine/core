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
import de.cubeisland.engine.messagecompositor.macro.PostProcessor;
import de.cubeisland.engine.messagecompositor.macro.Reader;

import static de.cubeisland.engine.core.util.ChatFormat.BASE_CHAR;
import static de.cubeisland.engine.core.util.ChatFormat.RESET;

public class ColorPostProcessor implements PostProcessor
{
    private final ChatFormat defaultColor;

    public ColorPostProcessor()
    {
        this(ChatFormat.GOLD);
    }

    public ColorPostProcessor(ChatFormat defaultColor)
    {
        this.defaultColor = defaultColor;
    }

    @Override
    public String process(String object, MacroContext context)
    {
        ChatFormat color = context.readMapped("color", ChatFormat.class);
        if (color == null)
        {
            color = defaultColor;
        }
        String source = context.getSourceMessage();
        if (source.length() > 2 && source.charAt(0) == BASE_CHAR)
        {
            ChatFormat byChar = ChatFormat.getByChar(source.charAt(1));
            if (byChar != null)
            {
                return color + ChatFormat.stripFormats(object) + byChar;
            }
        }
        return color + object + RESET;
    }

    public static class ColorReader implements Reader<ChatFormat>
    {
        @Override
        public ChatFormat read(String raw)
        {
            return ChatFormat.valueOf(raw.toUpperCase());
        }
    }
}
