package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.SubLogConfig;

public class BlockDecayConfig extends SubLogConfig
{
    public BlockDecayConfig() {
        super(false);
    }

    @Override
    public String getName()
    {
        return "block-decay";
    }
}