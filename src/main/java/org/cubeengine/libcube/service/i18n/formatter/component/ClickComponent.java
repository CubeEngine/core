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

import net.kyori.adventure.text.event.ClickEvent;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;

import java.net.URL;

public class ClickComponent implements Component
{
    private final ClickEvent click;
    private final Component component;

    private ClickComponent(ClickEvent click, Component component)
    {
        this.click = click;
        this.component = component;
    }

    public ClickEvent getClick()
    {
        return click;
    }

    public Component getComponent()
    {
        return component;
    }

    public static Component openURL(URL url, Component component)
    {
        return openURL(url.toString(), component);
    }

    public static Component openURL(String url, Component component)
    {
        return new ClickComponent(ClickEvent.openUrl(url), component);
    }

    public static Component runCommand(String command, Component component)
    {
        return new ClickComponent(ClickEvent.runCommand(command), component);
    }

    public static Component openURL(URL url, String text)
    {
        return openURL(url, new Text(text));
    }

    public static Component runCommand(String command, String text)
    {
        return runCommand(command, new Text(text));
    }

}
