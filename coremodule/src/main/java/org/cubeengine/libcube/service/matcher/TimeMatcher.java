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
package org.cubeengine.libcube.service.matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.cubeengine.libcube.util.StringUtils;

/**
 * A Matcher for ingame time
 */
@ServiceProvider(TimeMatcher.class)
public class TimeMatcher
{
    private Map<String, Long> times = new HashMap<>();

    @Inject private StringMatcher sm;

    public TimeMatcher()
    {
        times.put("day", 6000L);
        times.put("night", 18000L);
        /*
        0:
        sunrise
        morning
        3000:
        forenoon
        6000:
        day
        noon
        9000:
        afternoon
        13000:
        dusk
        moonrise
        15000:
        evening
        even
        sunset
        18000:
        night
        midnight
        22500:
        dawn
        */
    }

    public Long parseTime(String time)
    {
        if (time == null)
        {
            return null;
        }
        try
        {
            Matcher matcher = PARSE_TIME_PATTERN.matcher(time);
            // is it a formatted time?
            if (matcher.find())
            {
                // no null-check: group 1 is always available if matched
                String part = matcher.group(1);
                // remove leading zeros to prevent the number from being interpreted as an octal number
                if (part.charAt(0) == '0')
                {
                    part = part.substring(1);
                    if (part.isEmpty())
                    {
                        part = "0";
                    }
                }
                int hours = Integer.parseInt(part);
                int minutes = 0;
                // group 2: minutes; option
                part = matcher.group(2);
                if (part != null)
                {
                    if (part.charAt(0) == '0')
                    {
                        part = part.substring(1);
                    }
                    minutes = Integer.parseInt(part);
                }
                // if more than 60 minutes, hours will be added instead to keep the minutes below 60
                hours += minutes / 60;
                minutes %= 60;
                // keep the hours within 24 hours
                hours = (hours * TICKS_PER_HOUR) % TICKS_PER_DAY;

                // group 3: am/pm; optional
                part = matcher.group(3);
                if (part != null)
                {
                    // keep the ours within 12
                    if (hours > HALF_DAY)
                    {
                        hours %= HALF_DAY;
                    }
                    // if pm had half a day
                    if (part.equalsIgnoreCase("pm"))
                    {
                        hours += HALF_DAY;
                    }
                }
                // shift the time for 6 hours to match MC's sun light
                // subtracts 6 to shift the time, add 24 and modulo24 to keep it between 0 and 24
                hours = (hours - LIGHT_SHIFT + TICKS_PER_DAY) % TICKS_PER_DAY;

                // calculate the ticks
                return hours + Math.round(TICKS_TO_MINUTES * (double)minutes);
            }

            long daytime = Long.parseLong(time);
            if (daytime == 0)
            {
                return 0L;
            }
            // if the time is below 24 is probably meant to be interpreted as hours
            if (daytime < 24)
            {
                daytime *= 1000;
            }
            return daytime;
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    public String format(long time)
    {
        // shift the time back to show a matching to the light
        time = (time + LIGHT_SHIFT) % TICKS_PER_DAY;
        int hours = (int)(time / TICKS_PER_HOUR);
        int minutes = (int)Math.round((double)(time % TICKS_PER_HOUR) / TICKS_TO_MINUTES);

        return StringUtils.padLeft("" + hours, '0', 2) + ":" + StringUtils.padRight("" + minutes, '0', 2);
    }

    public final int TICKS_PER_HOUR = 1000;
    public final int TICKS_PER_DAY = 24 * TICKS_PER_HOUR;
    public final int HALF_DAY = TICKS_PER_DAY / 2;
    public final int LIGHT_SHIFT = HALF_DAY / 2;
    public final double TICKS_TO_MINUTES = (double)TICKS_PER_DAY / 1440D;
    private static final Pattern PARSE_TIME_PATTERN = Pattern.compile("^([012]?\\d)(?::(\\d{2}))?(pm|am)?$", Pattern.CASE_INSENSITIVE);

    public Long matchTimeValue(String time)
    {
        time = sm.matchString(time, times.keySet());
        if (time == null)
        {
            return null;
        }
        return times.get(time);
    }

    public String matchTimeName(Long time)
    {
        Entry<String, Long> match = null;
        time = time % 24000;
        for (Entry<String, Long> entry : times.entrySet())
        {
            if (match == null)
            {
                match = entry;
            }
            else if (Math.abs(match.getValue() - time) > Math.abs(entry.getValue() - time))
            {
                match = entry;
            }
        }
        return match.getKey();
    }
}
