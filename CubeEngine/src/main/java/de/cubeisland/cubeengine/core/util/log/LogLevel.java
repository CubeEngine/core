package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Level;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class LogLevel extends Level
{
    public static final Level ALL = Level.ALL;
    public static final Level OFF = Level.OFF;
    
    public static final Level ERROR = new LogLevel("ERROR", 1000);
    public static final Level WARN = new LogLevel("WARNING", 900);
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
}
