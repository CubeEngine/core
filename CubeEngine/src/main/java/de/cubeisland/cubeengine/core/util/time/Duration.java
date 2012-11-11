package de.cubeisland.cubeengine.core.util.time;

public class Duration
{
    /**
     * This class should be able to convert at least
     * 1ms 1millisec 1millisecond 2milliseconds
     * 1t 1tick 2ticks
     * 1s 1sec 1second 2seconds
     * 1m 1min 1minutes 2mins 2minutes
     * 1h 1hour 2hours
     * 1d 1day 2days
     * 
     * 1W 1week 2weeks
     * 1M 1month 2months
     * 1Y 1year 2years
     */
    
    private long time;

    public Duration(long time)
    {
        this.time = time;
    }
    
    public Duration(String timeString)
    {
        this.time = time;
    }
    
    public long toTicks()
    {
        return this.time / 50;
    }

    public long toTimeUnit(TimeUnit timeUnit)
    {
        return this.time / timeUnit.millisPerTimeUnit;
    }
    
    @Override
    public String toString()
    {
        return "";
        // TODO detect which unit would be the best and then convert to this one (or 2 (should be 2 near units))
    }
    
    public enum TimeUnit
    {
        MILLISECOND(1,"ms","millisec","millisecond","milliseconds"),
        TICK(50,"t","tick","tick","ticks"),
        SECOND(1000,"s","sec","second","seconds"),
        MINUTE(60000,"m","min","minute","minutes"),
        HOUR(3600000,"h","hour","hour","hours"),
        DAY(86400000,"d","day","day","days"),
        WEEK(604800000,"W","week","week","weeks"),
        MONTH(2592000000L,"M","month","month","months"),
        YEAR(31536000000L,"Y","year","year","years");

        private TimeUnit(long MillisPerTimeUnit, String veryShortName, String shortName, String longName, String longNamePlural)
        {
            this.millisPerTimeUnit = MillisPerTimeUnit;
            this.veryShortName = veryShortName;
            this.shortName = shortName;
            this.longName = longName;
            this.longNamePlural = longNamePlural;
        }
        
        public final long millisPerTimeUnit;
        public final String veryShortName;
        public final String shortName;
        public final String longName;
        public final String longNamePlural;
    }
    
    /*
    public static long convertTimeToMillis(String str) throws ConversionException
    {
        Pattern pattern = Pattern.compile("^(\\d+)([sSmhHdDwWMyY])?$");
        Matcher matcher = pattern.matcher(str);
        matcher.find();

        long time;
        try
        {
            time = Integer.parseInt(String.valueOf(matcher.group(1)));
        }
        catch (Exception e)
        {
            throw new ConversionException("Error while Converting String to time in millis");
        }
        if (time < 0)
        {
            return -1;
        }
        String unitSuffix = matcher.group(2);
        if (unitSuffix == null)
        {
            unitSuffix = "m";
        }
        switch (unitSuffix.charAt(0))
        {
            case 'y':
            case 'Y':
                time *= 365;
            case 'd':
            case 'D':
                time *= 24;
            case 'h':
            case 'H':
                time *= 60;
            case 'm':
                time *= 60;
            case 's':
            case 'S':
                time *= 1000;
                break;
            case 'W':
            case 'w':
                time *= 7 * DAY;
                break;
            case 'M':
                time *= 30 * DAY;
                break;
        }
        return time;
    }
     */
}
