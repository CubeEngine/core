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

import org.cubeengine.dirigent.context.Arguments;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.Text;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.text.NumberFormat;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

@Names({"amount", "integer", "long", "short", "decimal"})
public class NumberFormatter extends ReflectedFormatter {

    @Format
    public Component format(Integer i)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(i));
    }

    @Format
    public Component format(Long l)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(l));
    }

    @Format
    public Component format(Short s)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(s));
    }

    @Format
    public Component format(Double d, Context context, Arguments args)
    {
        return formatDecimal(d, context, args);
    }

    @Format
    public Component format(Float f, Context context, Arguments args)
    {
        return formatDecimal(f, context, args);
    }

    @Format
    public Component format(BigDecimal bd, Context context, Arguments args)
    {
        return formatDecimal(bd, context, args);
    }

    public Component formatDecimal(Number number, Context context, Arguments args)
    {
        NumberFormat format = NumberFormat.getInstance(context.get(LOCALE));
        String arg = args.get(0);
        if (arg == null)
        {
            return new Text(format.format(number));
        }
        Integer decimalPlaces = Integer.valueOf(arg);
        format.setMaximumFractionDigits(decimalPlaces);
        format.setMinimumFractionDigits(decimalPlaces);
        return new Text(format.format(number));
    }
}
