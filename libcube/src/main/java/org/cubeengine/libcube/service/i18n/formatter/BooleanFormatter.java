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

import de.cubeisland.engine.i18n.I18nService;
import org.cubeengine.dirigent.context.Arguments;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.AbstractFormatter;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

public class BooleanFormatter extends AbstractFormatter<Boolean>
{
    private final I18nService i18n;

    public BooleanFormatter(I18nService i18n)
    {
        super("bool");
        this.i18n = i18n;
    }

    @Override
    public Component format(Boolean object, Context context, Arguments args)
    {
        return new Text(i18n.translate(context.get(LOCALE), object ? "yes" : "no"));
    }
}
