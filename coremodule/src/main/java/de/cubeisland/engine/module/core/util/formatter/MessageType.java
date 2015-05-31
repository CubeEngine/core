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
package de.cubeisland.engine.module.core.util.formatter;

import java.awt.Color;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class MessageType implements TextColor.Base
{
    public final static MessageType POSITIVE = new MessageType(() -> TextColors.GREEN);
    public final static MessageType NEUTRAL = new MessageType(() -> TextColors.YELLOW);
    public final static MessageType NEGATIVE = new MessageType(() -> TextColors.RED);
    public final static MessageType CRITICAL = new MessageType(() -> TextColors.DARK_RED);
    public final static MessageType NONE = new MessageType(() -> TextColors.RESET);

    private ColorProvider color;

    private MessageType(ColorProvider provider)
    {
        this.color = provider;
    }

    @Override
    public Color getColor()
    {
        return this.color.getColor().getColor();
    }

    @Override
    public String getId()
    {
        return this.color.getColor().getId();
    }

    @Override
    public String getName()
    {
        return this.color.getColor().getName();
    }

    @Override
    public char getCode()
    {
        return color.getColor().getCode();
    }

    public void setColor(TextColor.Base color)
    {
        this.color = () -> color;
    }

    public interface ColorProvider
    {
        TextColor.Base getColor();
    }
}
