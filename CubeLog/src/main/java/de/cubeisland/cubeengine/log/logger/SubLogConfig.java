package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;

public abstract class SubLogConfig extends Configuration
{
    @Option(value = "enabled")
    public boolean enabled = false;
    
    public abstract String getName();
}
