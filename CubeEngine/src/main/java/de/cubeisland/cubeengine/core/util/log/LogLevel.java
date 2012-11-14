package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Level;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public class LogLevel extends Level
{
    public static final Level ALL = LogLevel.ALL;
    public static final Level OFF = LogLevel.OFF;
    public static final Level ERROR = new LogLevel("ERROR", 1000);
    public static final Level WARNING = new LogLevel("WARNING", 900);
    public static final Level NOTICE = new LogLevel("NOTICE", 800);
    public static final Level INFO = new LogLevel("INFO", 700);
    public static final Level DEBUG = new LogLevel("DEBUG", 600);

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
        return null;//TODO
    }
}
