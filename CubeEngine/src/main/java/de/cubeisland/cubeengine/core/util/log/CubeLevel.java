package de.cubeisland.cubeengine.core.util.log;

import java.util.Locale;
import java.util.logging.Level;

public class CubeLevel extends Level
{

    CubeLevel(String name, int level)
    {
        super(name, level);
        LogLevel.LEVELS.put(name.toUpperCase(Locale.ENGLISH), this);
    }
}
