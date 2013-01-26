package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

public class BlockFormConfig extends SubLogConfig
{
    public BlockFormConfig()
    {
        super(false);
    }

    @Option("log-snow-form")
    public boolean logSnowForm = false;
    @Option("log-ice-form")
    public boolean logIceForm = false;

    @Override
    public String getName()
    {
        return "block-form";
    }
}