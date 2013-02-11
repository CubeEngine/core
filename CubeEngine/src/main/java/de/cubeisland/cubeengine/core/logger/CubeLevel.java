package de.cubeisland.cubeengine.core.logger;

import java.util.Locale;
import java.util.logging.Level;

public class CubeLevel extends Level
{
    CubeLevel(String name, int level)
    {
        super(name, level);
        LogLevel.LEVELS.put(name.toUpperCase(Locale.ENGLISH), this);
    }

    CubeLevel(Level level)
    {
        super(level.getName(), level.intValue(), level.getResourceBundleName());
        LogLevel.LEVELS.put(this.getName().toUpperCase(Locale.ENGLISH), this);
    }
}
