package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.LoggerConfig;

public class BlockBurnConfig extends LoggerConfig
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