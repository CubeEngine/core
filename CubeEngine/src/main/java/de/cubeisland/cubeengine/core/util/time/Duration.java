package de.cubeisland.cubeengine.core.util.time;

import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TCharLongHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Duration
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
    private long time;
    private long week;
    private long day;
    private long hour;
    private long minute;
    private long second;
    private long milsec;

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
        this.time = parseDuration(timeStrings);
    }

    public Duration(long duration)
    {
        this.time = duration;
    }

    public Duration(long from, long to)
    {
        this.time = to - from < 0 ? from - to : to - from;
    }

    public Duration(String timeString)
    {
        this(StringUtils.explode(" ", timeString));
    }

    public static long parseDuration(String[] timeStrings) throws IllegalArgumentException
    {
        long result = 0;
        if (timeStrings.length == 1 && timeStrings[0].equals("-1"))
        {
            return -1;
        }
        for (int i = 0; i < timeStrings.length; ++i)
        {
            String timeString = timeStrings[i];
            long time = 0;
            Long timeUnitFactor = null;
            try
            {
                if (timeStrings.length <= i + 1)
                {
                    throw new NumberFormatException(); // not realy but i want do avoid IndexOutOfBounds
                }
                time = Long.parseLong(timeString);
                String timeUnitString = timeStrings[i + 1];
                String unit = Match.string().matchString(timeUnitString, longerNames.keySet());
                if (unit == null)
                {
                    List<String> matches = Match.string().getBestMatches(timeUnitString, shortNames.keySet(), 1);
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

    public boolean isInfinite()
    {
        return this.time == -1;
    }

    public long toTicks()
    {
        return this.time / 50;
    }

    public long toMillis()
    {
        return this.time;
    }

    public long toTimeUnit(TimeUnit timeUnit)
    {
        return timeUnit.convert(this.time, TimeUnit.MILLISECONDS);
    }

    public String format() // good for config
    {
        //search greatest unit:
        if (this.time == -1)
        {
            return "-1";
        }
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

    private void update()
    {
        long convertTime = this.time;
        this.week = convertTime / WEEK;
        convertTime -= convertTime / WEEK * WEEK;
        this.day = convertTime / DAY;
        convertTime -= convertTime / DAY * DAY;
        this.hour = convertTime / HOUR;
        convertTime -= convertTime / HOUR * HOUR;
        this.minute = convertTime / MIN;
        convertTime -= convertTime / MIN * MIN;
        this.second = convertTime / SEC;
        convertTime -= convertTime / SEC * SEC;
        this.milsec = convertTime / MS;
        convertTime -= convertTime / MS * MS;
    }

    //TODO format method for chat output! DO IT BETTER.
    public String format(String pattern)
    {
        this.update();
        String result = pattern;
        if (result.contains("%www") || result.contains("%ww") || result.contains("%w"))
        {
            result = result.replaceAll("%www", week == 0 ? "" : week + (week == 1 ? " week" : " weeks"));
            result = result.replaceAll("%ww", week == 0 ? "" : week + "w");
            result = result.replaceAll("%w", week == 0 ? "" : week + "");
        }
        else
        {
            day += week * 7;
        }
        if (result.contains("%ddd") || result.contains("%dd") || result.contains("%d"))
        {
            result = result.replaceAll("%ddd", day == 0 ? "" : day + (day == 1 ? " day" : " days"));
            result = result.replaceAll("%dd", day == 0 ? "" : day + "d");
            result = result.replaceAll("%d", day == 0 ? "" : day + "");
        }
        else
        {
            hour += hour * 24;
        }
        if (result.contains("%hhh") || result.contains("%hh") || result.contains("%h"))
        {
            result = result.replaceAll("%hhh", hour == 0 ? "" : hour + (hour == 1 ? " hour" : " hours"));
            result = result.replaceAll("%hh", hour == 0 ? "" : hour + "h");
            result = result.replaceAll("%h", hour == 0 ? "" : hour + "");
        }
        else
        {
            minute += minute * 60;
        }
        if (result.contains("%mmm") || result.contains("%mm") || result.contains("%m"))
        {
            result = result.replaceAll("%mmm", minute == 0 ? "" : minute + (minute == 1 ? " minute" : " minutes"));
            result = result.replaceAll("%mm", minute == 0 ? "" : minute + "min");
            result = result.replaceAll("%m", minute == 0 ? "" : minute + "");
        }
        else
        {
            second += second * 60;
        }
        if (result.contains("%sss") || result.contains("%ss") || result.contains("%s"))
        {
            result = result.replaceAll("%sss", second == 0 ? "" : second + (second == 1 ? " second" : " seconds"));
            result = result.replaceAll("%ss", second == 0 ? "" : second + "sec");
            result = result.replaceAll("%s", second == 0 ? "" : second + "");
        }
        else
        {
            milsec += milsec * 60;
        }
        if (result.contains("%SSS") || result.contains("%SSS") || result.contains("%SSS"))
        {
            result = result.replaceAll("%SSS", milsec == 0 ? "" : milsec + (milsec == 1 ? " millisecond" : " milliseconds"));
            result = result.replaceAll("%SSS", milsec == 0 ? "" : milsec + "ms");
            result = result.replaceAll("%SSS", milsec == 0 ? "" : milsec + "");
        }
        return result;
    }
}
