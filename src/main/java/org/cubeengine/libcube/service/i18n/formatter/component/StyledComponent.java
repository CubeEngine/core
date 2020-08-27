/*
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
package org.cubeengine.libcube.service.i18n.formatter.component;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;

public class StyledComponent implements Component
{
    private final Style format;
    private final Component component;

    public StyledComponent(Style format, Component component)
    {
        this.format = format;
        this.component = component;
    }

    public StyledComponent(TextColor format, Component component)
    {
        this.format = Style.empty().color(format);
        this.component = component;
    }

    public StyledComponent(TextColor format, String text)
    {
        this.format = Style.empty().color(format);
        this.component = new Text(text);
    }

    public StyledComponent(Style format, String text)
    {
        this.format = format;
        this.component = new Text(text);
    }

    public Style getFormat()
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

    public static Component styled(Style style, Component component)
    {
        return new StyledComponent(style, component);
    }

    public static Component colored(TextColor color, String text)
    {
        return new StyledComponent(color, text);
    }

    public static Component styled(Style style, String text)
    {
        return new StyledComponent(style, text);
    }
}
