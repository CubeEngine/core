package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.SubLogConfig;

public class BlockBurnConfig extends SubLogConfig
{
    public BlockBurnConfig() {
        super(false);
    }

    @Override
    public String getName()
    {
        return "block-burn";
    }
}