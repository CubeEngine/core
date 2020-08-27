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


import net.kyori.adventure.text.TranslatableComponent;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.TextComponent;

@Names({"name", "input", "message", "txt"})
public class StringFormatter extends ReflectedFormatter
{
    @Format
    public Component format(String string)
    {
        return new Text(string);
    }

    @Format
    public Component format(TranslatableComponent translation, Context context)
    {
        return new TextComponent(translation);
    }

    @Format
    public Component format(net.kyori.adventure.text.Component text)
    {
        return new TextComponent(text);
    }
}
