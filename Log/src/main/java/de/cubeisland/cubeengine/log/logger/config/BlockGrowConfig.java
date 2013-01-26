package de.cubeisland.cubeengine.log.logger.config;


import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

public  class BlockGrowConfig extends SubLogConfig
{
    public BlockGrowConfig()
    {
        super(false);
    }

    @Option("log-natural-grow")
    public boolean logNatural = false;
    @Option("log-player-grow")
    public boolean logPlayer = true;

    @Override
    public String getName()
    {
        return "block-grow";
    }
}