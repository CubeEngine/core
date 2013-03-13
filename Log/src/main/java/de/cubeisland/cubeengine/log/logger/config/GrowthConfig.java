package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public class GrowthConfig extends LoggerConfig
{
    public GrowthConfig()
    {
        super(false);
    }

    @Option("log-natural-grow")
    public boolean logNatural = false;
    @Option("log-player-grow")
    public boolean logPlayer = true;

    @Option("log-fire-spread")
    public boolean logFireSpread = true;
    @Option("log-other-spread")
    public boolean logOtherSpread = true;

    @Override
    public String getName()
    {
        return "block-grow";
    }
}
