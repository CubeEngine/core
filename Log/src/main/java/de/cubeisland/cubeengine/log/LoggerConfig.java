package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;

public abstract class LoggerConfig extends Configuration
{
    protected LoggerConfig(boolean enabled) {
        this.enabled = enabled;
    }

    @Option("enabled")
    public boolean enabled;

    public abstract String getName();
}
