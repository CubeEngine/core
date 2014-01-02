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
package de.cubeisland.engine.core.util;

import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;

public class TimeUtil
{
    public static String format(Locale locale, long duration)
    {
        PrettyTime format = new PrettyTime(locale);
        format.getUnit(JustNow.class).setMaxQuantity(1000L);
        return format.format(format.calculatePreciseDuration(new Date(duration + System.currentTimeMillis())));
    }

    public static String format(Locale locale, Date timeFromNow)
    {
        PrettyTime format = new PrettyTime(locale);
        format.getUnit(JustNow.class).setMaxQuantity(1000);
        // TODO remove Milliseconds.class https://github.com/ocpsoft/prettytime/issues/56
        return format.format(format.calculatePreciseDuration(timeFromNow));
    }
}
