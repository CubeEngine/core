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

import java.net.URL;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;

public class ClickComponent implements Component
{
    private final Object click;
    private final Component component;

    private ClickComponent(Object hover, Component component)
    {
        this.click = hover;
        this.component = component;
    }

    public ClickComponent(Object hover, String text)
    {
        this(hover, new Text(text));
    }

    public Object getClick()
    {
        return click;
    }

    public Component getComponent()
    {
        return component;
    }

    public static Component openURL(URL url, Component component)
    {
        return new ClickComponent(url, component);
    }

    public static Component runCommand(String command, Component component)
    {
        return new ClickComponent(command, component);
    }

    public static Component openURL(URL url, String text)
    {
        return new ClickComponent(url, text);
    }

    public static Component runCommand(String command, String text)
    {
        return new ClickComponent(command, text);
    }

}
