package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.LoggerConfig;

public class BlockDecayConfig extends LoggerConfig
{
    public BlockDecayConfig()
    {
        super(false);
    }

    @Override
    public String getName()
    {
        return "block-decay";
    }
}
