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

public class MessageType
{
    public final static MessageType POSITIVE = new MessageType(ChatFormat.BRIGHT_GREEN);
    public final static MessageType NEUTRAL = new MessageType(ChatFormat.YELLOW);
    public final static MessageType NEGATIVE = new MessageType(ChatFormat.RED);
    public final static MessageType CRITICAL = new MessageType(ChatFormat.DARK_RED);
    public final static MessageType NONE = new MessageType(null);

    private ChatFormat color;

    private MessageType(ChatFormat color)
    {
        this.color = color;
    }

    public String getColorCode()
    {
        if (color == null)
        {
            return "";
        }
        return this.color.toString();
    }
}
