package de.cubeisland.cubeengine.core.util.log;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public class LogLevel extends Level
{
    private static final ConcurrentMap<String, LogLevel> levels = new ConcurrentHashMap<String, LogLevel>(7);
    public static final LogLevel ALL = new LogLevel("ALL", Integer.MIN_VALUE);
    public static final LogLevel OFF = new LogLevel("OFF", Integer.MAX_VALUE);
    public static final LogLevel ERROR = new LogLevel("Error", 1000);
    public static final LogLevel WARNING = new LogLevel("Warning", 900);
    public static final LogLevel NOTICE = new LogLevel("Notice", 800);
    public static final LogLevel INFO = new LogLevel("Info", 700);
    public static final LogLevel DEBUG = new LogLevel("Debug", 600);

    private LogLevel(String name, int level)
    {
        super(name, level);
        levels.put(this.getName().toUpperCase(Locale.ENGLISH), this);
    }

    public static LogLevel parse(String name)
    {
        return levels.get(name.toUpperCase(Locale.ENGLISH));
    }
}
