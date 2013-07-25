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
package de.cubeisland.engine.core.util.time;

public abstract class Time
{
    long time; // no access modifier is inteded!

    public Time(long time)
    {
        this.time = time;
    }
    /*
     * public enum TimeUnit
     * {
     * MILLISECOND(1, 'S', "ms", "millisec", "millisecond", "milliseconds"),
     * TICK(50, 't', "t", "tick", "tick", "ticks"),
     * SECOND(1000, 's', "s", "sec", "second", "seconds"),
     * MINUTE(60000, 'm', "m", "min", "minute", "minutes"),
     * HOUR(3600000, 'h', "h", "hour", "hour", "hours"),
     * DAY(86400000, 'd', "d", "day", "day", "days"),
     * WEEK(604800000, 'W', "W", "week", "week", "weeks"),
     * MONTH(2592000000L, 'M', "M", "month", "month", "months"),
     * YEAR(31536000000L, 'Y', "Y", "year", "year", "years");
     * public final long millisPerTimeUnit;
     * public final char character;
     * public final String veryShortName;
     * public final String shortName;
     * public final String longName;
     * public final String longNamePlural;
     *
     * private TimeUnit(long MillisPerTimeUnit, char character, String veryShortName, String shortName, String longName, String longNamePlural)
     * {
     * this.millisPerTimeUnit = MillisPerTimeUnit;
     * this.character = character;
     * this.veryShortName = veryShortName;
     * this.shortName = shortName;
     * this.longName = longName;
     * this.longNamePlural = longNamePlural;
     * }
     * }// */
}
