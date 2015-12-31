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
package org.cubeengine.service.i18n.formatter.component;

import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.parser.component.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;

public class StyledComponent implements Component
{
    private final TextFormat format;
    private final Component component;

    public StyledComponent(TextFormat format, Component component)
    {
        this.format = format;
        this.component = component;
    }

    public StyledComponent(TextColor format, Component component)
    {
        this.format = TextFormat.NONE.color(format);
        this.component = component;
    }

    public StyledComponent(TextStyle format, Component component)
    {
        this.format = TextFormat.NONE.style(format);
        this.component = component;
    }

    public StyledComponent(TextFormat format, String text)
    {
        this(format, new Text(text));
    }

    public StyledComponent(TextColor format, String text)
    {
        this.format = TextFormat.NONE.color(format);
        this.component = new Text(text);
    }

    public StyledComponent(TextStyle format, String text)
    {
        this.format = TextFormat.NONE.style(format);
        this.component = new Text(text);
    }


    public TextFormat getFormat()
    {
        return format;
    }

    public Component getComponent()
    {
        return component;
    }

    public static Component colored(TextColor color, Component component)
    {
        return new StyledComponent(color, component);
    }

    public static Component styled(TextStyle style, Component component)
    {
        return new StyledComponent(style, component);
    }

    public static Component colored(TextColor color, String text)
    {
        return new StyledComponent(color, text);
    }

    public static Component styled(TextStyle style, String text)
    {
        return new StyledComponent(style, text);
    }
}
