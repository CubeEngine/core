package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public class FireConfig extends LoggerConfig
{
    public FireConfig()
    {
        super(false);
    }

    @Option("log-block-burn")
    public boolean blockBurn = true;
    @Option("log-lava-fire-spread")
    public boolean lavaFireSpread = true;
    @Option("log-fire-spread")
    public boolean fireSpread = true;
    @Option("log-flint-and-steel-ignite")
    public boolean flintAndSteelIgnite = true;
    @Option("log-lightning-ignite")
    public boolean lightningIgnite = true;
    @Option("log-fireball-ignite")
    public boolean fireballIgnite = true;

    @Override
    public String getName()
    {
        return "block-burn";
    }
}
