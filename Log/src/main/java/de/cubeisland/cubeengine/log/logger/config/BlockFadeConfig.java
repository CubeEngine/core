package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

public class BlockFadeConfig extends SubLogConfig
{
    public BlockFadeConfig()
    {
        super(false);
    }

    @Option("log-snow-melt")
    public boolean logSnowMelt = false;
    @Option("log-ice-melt")
    public boolean logIceMelt = false;

    @Override
    public String getName()
    {
        return "block-fade";
    }
}