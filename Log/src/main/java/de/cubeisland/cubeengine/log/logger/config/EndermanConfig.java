package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

public class EndermanConfig extends SubLogConfig
{
    public EndermanConfig() {
        super(false);
    }

    @Option("log-enderman-place")
    public boolean logPlace = false;
    @Option("log-enderman-take")
    public boolean logTake = false;

    @Override
    public String getName()
    {
        return "enderman";
    }
}