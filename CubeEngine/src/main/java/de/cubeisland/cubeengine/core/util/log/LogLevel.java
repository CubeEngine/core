package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Level;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public class LogLevel extends Level
{
    public static final LogLevel ALL     = new LogLevel("ALL", Integer.MIN_VALUE);
    public static final LogLevel OFF     = new LogLevel("OFF", Integer.MAX_VALUE);
    public static final LogLevel ERROR   = new LogLevel("ERROR", 1000);
    public static final LogLevel WARNING = new LogLevel("WARNING", 900);
    public static final LogLevel NOTICE  = new LogLevel("NOTICE", 800);
    public static final LogLevel INFO    = new LogLevel("INFO", 700);
    public static final LogLevel DEBUG   = new LogLevel("DEBUG", 600);

    private LogLevel(String name, int level)
    {
        super(name, level);
    }

    @Override
    public String getLocalizedName()
    {
        return _("core", this.getName());
    }

    public static LogLevel parse(String name)
    {
        if (name.equalsIgnoreCase("ALL"))
        {
            return LogLevel.ALL;
        }
        if (name.equalsIgnoreCase("OFF"))
        {
            return LogLevel.OFF;
        }
        if (name.equalsIgnoreCase("ERROR"))
        {
            return LogLevel.ERROR;
        }
        if (name.equalsIgnoreCase("WARNING"))
        {
            return LogLevel.WARNING;
        }
        if (name.equalsIgnoreCase("NOTICE"))
        {
            return LogLevel.NOTICE;
        }
        if (name.equalsIgnoreCase("INFO"))
        {
            return LogLevel.INFO;
        }
        if (name.equalsIgnoreCase("DEBUG"))
        {
            return LogLevel.DEBUG;
        }
        return null;
    }
}
