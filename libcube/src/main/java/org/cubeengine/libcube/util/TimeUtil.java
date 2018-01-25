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
package org.cubeengine.libcube.util;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.time.Duration;
import java.util.Date;
import java.util.Locale;

public class TimeUtil
{
    public static String format(Locale locale, long duration)
    {
        PrettyTime format = new PrettyTime(locale);
        format.getUnit(JustNow.class).setMaxQuantity(1000L);
        return format.format(format.calculatePreciseDuration(new Date(duration + System.currentTimeMillis())));
    }

    public static String formatDuration(long time)
    {
        Duration d = Duration.ofMillis(time);
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        long minutes = d.minusDays(days).minusHours(hours).toMinutes();
        long seconds = d.minusDays(days).minusHours(hours).minusMinutes(minutes).toMillis() / 1000;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" d");
        }
        if (hours > 0) {
            sb.append(hours).append(" h");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" m");
        }
        if (seconds > 0) {
            sb.append(seconds).append(" s");
        }
        String result = sb.toString();
        if (result.isEmpty()) {
            result = "0";
        }
        return result;
    }

    public static String format(Locale locale, Date timeFromNow)
    {
        PrettyTime format = new PrettyTime(locale);
        //format.getUnit(JustNow.class).setMaxQuantity(1000);
        format.removeUnit(JustNow.class);
        format.removeUnit(Millisecond.class);
        return format.format(format.calculatePreciseDuration(timeFromNow));
    }
}
