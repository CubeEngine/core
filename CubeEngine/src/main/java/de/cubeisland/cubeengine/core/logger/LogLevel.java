package de.cubeisland.cubeengine.core.logger;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public final class LogLevel
{
    static final ConcurrentMap<String, CubeLevel> LEVELS = new ConcurrentHashMap<String, CubeLevel>(7);

    public static final CubeLevel ALL = new CubeLevel(Level.ALL);
    public static final CubeLevel OFF = new CubeLevel(Level.OFF);

    public static final CubeLevel ERROR = new CubeLevel("Error", 10000);
    public static final CubeLevel WARNING = new CubeLevel("Warning", 9000);
    public static final CubeLevel NOTICE = new CubeLevel("Notice", 8000);
    public static final CubeLevel INFO = new CubeLevel("Info", 7000);
    public static final CubeLevel DEBUG = new CubeLevel("Debug", 6000);

    private LogLevel()
    {}

    public static CubeLevel parse(String name)
    {
        return LEVELS.get(name.toUpperCase(Locale.ENGLISH));
    }

}
