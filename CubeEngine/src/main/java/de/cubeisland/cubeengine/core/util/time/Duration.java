package de.cubeisland.cubeengine.core.util.time;

import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.TCharLongHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Duration extends Time
{
    private final static TObjectLongHashMap<String> longerNames = new TObjectLongHashMap<String>();
    private final static TObjectLongHashMap<String> shortNames = new TObjectLongHashMap<String>();
    private final static TCharLongHashMap veryShortNames = new TCharLongHashMap();
    private final static long MS = TimeUnit.MILLISECONDS.toMillis(1L);
    private final static long SEC = TimeUnit.SECONDS.toMillis(1L);
    private final static long MIN = TimeUnit.MINUTES.toMillis(1L);
    private final static long HOUR = TimeUnit.HOURS.toMillis(1L);
    private final static long DAY = TimeUnit.DAYS.toMillis(1L);
    private final static long WEEK = TimeUnit.DAYS.toMillis(1L) * 7;

    static
    {
        veryShortNames.put('S', MS);
        veryShortNames.put('s', SEC);
        veryShortNames.put('m', MIN);
        veryShortNames.put('h', HOUR);
        veryShortNames.put('d', DAY);
        veryShortNames.put('w', WEEK);

        shortNames.put("ms", MS);
        shortNames.put("sec", SEC);
        shortNames.put("min", MIN);

        longerNames.put("milliseconds", MS);
        longerNames.put("seconds", SEC);
        longerNames.put("minutes", MIN);
        longerNames.put("hours", HOUR);
        longerNames.put("days", DAY);
        longerNames.put("weeks", WEEK);
    }

    public Duration(String[] timeStrings)
    {
        super(parseDuration(timeStrings));
    }

    public static long parseDuration(String[] timeStrings) throws IllegalArgumentException
    {
        long result = 0;
        for (int i = 0; i < timeStrings.length; ++i)
        {
            String timeString = timeStrings[i];
            long time = 0;
            Long timeUnitFactor = null;
            try
            {
                time = Long.parseLong(timeString);
                String timeUnitString = timeStrings[i + 1];
                String unit = StringUtils.matchString(timeUnitString, longerNames.keySet());
                if (unit == null)
                {
                    List<String> matches = StringUtils.getBestMatches(timeUnitString, shortNames.keySet(), 1);
                    if (!matches.isEmpty())
                    {
                        timeUnitFactor = shortNames.get(matches.get(0));
                    }
                }
                else
                {
                    timeUnitFactor = longerNames.get(unit);
                }
                i++;
            }
            catch (NumberFormatException e) // now try 1d, 1m etc. values
            {
                for (Character c : veryShortNames.keys())
                {
                    if (timeString.endsWith(c.toString()))
                    {
                        try
                        {
                            time = Long.parseLong(timeString.substring(0, timeString.length() - 1));
                            timeUnitFactor = veryShortNames.get(c);
                        }
                        catch (NumberFormatException ex)
                        {
                            break;
                        }
                    }
                }
                if (timeString.endsWith("ms"))
                {
                    try
                    {
                        time = Long.parseLong(timeString.substring(0, timeString.length() - 2));
                        timeUnitFactor = MS;
                    }
                    catch (NumberFormatException ex)
                    {
                        break;
                    }
                }
            }
            if (timeUnitFactor == null)
            {
                throw new IllegalArgumentException("Could not parse time! " + timeStrings.toString());
            }
            result += time * timeUnitFactor;
        }
        return result;
    }

    public long toTicks()
    {
        return this.time / 50;
    }

    public long toTimeUnit(TimeUnit timeUnit)
    {
        return timeUnit.convert(this.time, TimeUnit.MILLISECONDS);
    }

    public String format() // good for config
    {
        //search greatest unit:
        long convertTime = this.time;
        StringBuilder sb = new StringBuilder();
        while (convertTime > 0)
        {
            if (convertTime / WEEK > 0)
            {
                sb.append(convertTime / WEEK).append("w ");
                convertTime -= convertTime / WEEK * WEEK;
            }
            else if (convertTime / DAY > 0)
            {
                sb.append(convertTime / DAY).append("d ");
                convertTime -= convertTime / DAY * DAY;
            }
            else if (convertTime / HOUR > 0)
            {
                sb.append(convertTime / HOUR).append("h ");
                convertTime -= convertTime / HOUR * HOUR;
            }
            else if (convertTime / MIN > 0)
            {
                sb.append(convertTime / MIN).append("m ");
                convertTime -= convertTime / MIN * MIN;
            }
            else if (convertTime / SEC > 0)
            {
                sb.append(convertTime / SEC).append("s ");
                convertTime -= convertTime / SEC * SEC;
            }
            else if (convertTime / MS > 0)
            {
                sb.append(convertTime / MS).append("ms ");
                convertTime -= convertTime / MS * MS;
            }
            else
            {
                return sb.toString();
            }
        }
        return sb.toString();
    }
    //TODO format method for chat output!
}
