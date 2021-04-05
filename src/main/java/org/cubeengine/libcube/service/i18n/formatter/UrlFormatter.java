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
package org.cubeengine.libcube.service.i18n.formatter;

import java.net.URI;
import java.net.URL;
import java.util.Set;
import com.sun.org.apache.xpath.internal.Arg;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cubeengine.dirigent.context.Arguments;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.ConstantFormatter;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.ClickComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;

import static java.util.Collections.singleton;

@Names({"url", "uri"})
public class UrlFormatter extends ReflectedFormatter
{
    @Format
    public Component format(URL url, Arguments args)
    {
        return formatUrl(url.toString(), args);
    }

    @Format
    public Component format(URI url, Arguments args)
    {
        return formatUrl(url.toString(), args);
    }

    @Format
    public Component format(String url, Arguments args)
    {
        return formatUrl(url, args);
    }

    private static Component formatUrl(String url, Arguments args)
    {
        String label = args.getOrElse(0, url);
        String hover = args.getOrElse(1, url);
        return ClickComponent.openURL(url, HoverComponent.hoverText(hover, Text.create(label)));
    }
}
