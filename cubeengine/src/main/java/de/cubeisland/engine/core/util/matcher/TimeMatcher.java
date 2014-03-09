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
package de.cubeisland.engine.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.util.StringUtils;

/**
 * A Matcher for ingame time
 */
public class TimeMatcher
{

    private final Map<Long, List<String>> timeToName;
    private final Map<String, Long> nameToTime;

    public TimeMatcher()
    {
        timeToName = this.loadFromFile();
        nameToTime = new HashMap<>();
        for (Entry<Long, List<String>> entry : timeToName.entrySet())
        {
            for (String name : entry.getValue())
            {
                Long old = nameToTime.put(name, entry.getKey());
                if (old != null)
                {
                    CubeEngine.getLog().warn("Duplicate Time-Name \"{}\" for values: ({}|{})",
                                             name, old, entry.getKey());
                }
            }
        }
    }

    private Map<Long, List<String>> loadFromFile()
    {
        try
        {
            Path file = CubeEngine.getFileManager().getDataPath().resolve(CoreResource.TIMES.getTarget());
            List<String> input = FileUtil.readStringList(file);
            Map<Long,List<String>> readTime = new TreeMap<>();
            this.loadFromFile(readTime, input);
            try (InputStream is = CubeEngine.getFileManager().getSourceOf(file))
            {
                List<String> jarinput = FileUtil.readStringList(is);
                if (jarinput != null && this.loadFromFile(readTime, jarinput))
                {
                    CubeEngine.getLog().info("Updated times.txt");
                    StringBuilder sb = new StringBuilder();
                    for (Long timeValue : readTime.keySet())
                    {
                        sb.append(timeValue).append(":").append("\n");
                        for (String name : readTime.get(timeValue))
                        {
                            sb.append("  ").append(name).append("\n");
                        }
                    }
                    FileUtil.saveFile(sb.toString(), file);
                }
            }
            return readTime;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("items.txt is corrupted!", ex);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading items.txt", ex);
        }
    }

    private boolean loadFromFile(Map<Long, List<String>> readTime, List<String> input)
    {
        boolean update = !readTime.isEmpty();
        boolean updated = false;
        List<String> names = new ArrayList<>();
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.contains(":"))
            {
                Long data = Long.parseLong(line.substring(0, line.indexOf(":")));
                names = readTime.get(data);
                if (names == null)
                {
                    names = new ArrayList<>();
                    readTime.put(data, names);
                    updated = true;
                }
            }
            else
            {
                if (!names.contains(line))
                {
                    names.add(line);
                    updated = true;
                }
            }
        }
        return updated && update;
    }

    public Long getNearTime(Long timeValue)
    {
        timeValue = timeValue % 24000;
        long minDiff = 24000;
        Long near = null;
        long diff;
        for (Long time : this.timeToName.keySet())
        {
            diff = Math.abs(timeValue - time);
            if (diff < minDiff)
            {
                minDiff = diff;
                near = time;
            }
        }
        return near;
    }

    public String getNearTimeName(Long timeValue)
    {
        List<String> strings = this.timeToName.get(this.getNearTime(timeValue));
        if (strings == null || strings.isEmpty())
        {
            return null;
        }
        return strings.get(0);
    }


    /**
     * Match names first then try with to parse time
     *
     * @param timeName
     * @return
     */
    public Long matchTimeValue(String timeName)
    {
        String match = Match.string().matchString(timeName, this.nameToTime.keySet());
        if (match == null)
        {
            return this.parseTime(timeName);
        }
        return this.nameToTime.get(match);
    }

    /**
     * Parse time
     * @param time
     * @return
     */
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
}
