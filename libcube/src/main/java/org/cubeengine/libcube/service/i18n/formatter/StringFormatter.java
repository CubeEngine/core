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


import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.argument.Arguments;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.libcube.service.i18n.formatter.component.TextComponent;
import org.spongepowered.api.text.translation.Translation;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

@Names({"name","input","message", "txt"})
public class StringFormatter extends ReflectedFormatter
{
    @Format
    public Component format(String string)
    {
        return new Text(string);
    }

    @Format
    public Component format(Translation translation, Context context)
    {
        return new Text(translation.get(context.get(LOCALE)));
    }

    @Format
    public Component format(org.spongepowered.api.text.Text text)
    {
        return new TextComponent(text);
    }
}
