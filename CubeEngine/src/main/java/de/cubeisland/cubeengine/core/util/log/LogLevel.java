package de.cubeisland.cubeengine.core.util.log;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public final class LogLevel
{
    static final ConcurrentMap<String, CubeLevel> LEVELS = new ConcurrentHashMap<String, CubeLevel>(7);

    public static final CubeLevel ALL = new CubeLevel("ALL", Integer.MIN_VALUE);
    public static final CubeLevel OFF = new CubeLevel("OFF", Integer.MAX_VALUE);

    public static final CubeLevel ERROR = new CubeLevel("Error", 1000);
    public static final CubeLevel WARNING = new CubeLevel("Warning", 900);
    public static final CubeLevel NOTICE = new CubeLevel("Notice", 800);
    public static final CubeLevel INFO = new CubeLevel("Info", 700);
    public static final CubeLevel DEBUG = new CubeLevel("Debug", 600);

    private LogLevel()
    {}

    public static CubeLevel parse(String name)
    {
        return LEVELS.get(name.toUpperCase(Locale.ENGLISH));
    }

}
