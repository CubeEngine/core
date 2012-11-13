package de.cubeisland.cubeengine.core.util.time;

public abstract class Time
{
    long time; // no access modifier is inteded!

    public Time(long time)
    {
        this.time = time;
    }
/*
    public enum TimeUnit
    {
        MILLISECOND(1, 'S', "ms", "millisec", "millisecond", "milliseconds"),
        TICK(50, 't', "t", "tick", "tick", "ticks"),
        SECOND(1000, 's', "s", "sec", "second", "seconds"),
        MINUTE(60000, 'm', "m", "min", "minute", "minutes"),
        HOUR(3600000, 'h', "h", "hour", "hour", "hours"),
        DAY(86400000, 'd', "d", "day", "day", "days"),
        WEEK(604800000, 'W', "W", "week", "week", "weeks"),
        MONTH(2592000000L, 'M', "M", "month", "month", "months"),
        YEAR(31536000000L, 'Y', "Y", "year", "year", "years");
        public final long millisPerTimeUnit;
        public final char character;
        public final String veryShortName;
        public final String shortName;
        public final String longName;
        public final String longNamePlural;

        private TimeUnit(long MillisPerTimeUnit, char character, String veryShortName, String shortName, String longName, String longNamePlural)
        {
            this.millisPerTimeUnit = MillisPerTimeUnit;
            this.character = character;
            this.veryShortName = veryShortName;
            this.shortName = shortName;
            this.longName = longName;
            this.longNamePlural = longNamePlural;
        }
    }//*/
}
