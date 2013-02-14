package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public class BlockExplosionConfig extends LoggerConfig
{
    public BlockExplosionConfig()
    {
        super(true);
    }

    @Option("log-creeper-as-player-who-triggered")
    public boolean logCreeperAsPlayer = false;
    @Option("log-explosion-type.misc")
    public boolean logMisc = false;
    @Option("log-explosion-type.creeper")
    public boolean logCreeper = true;
    @Option("log-explosion-type.tnt")
    public boolean logTNT = true;
    @Option("log-explosion-type.ender-dragon")
    public boolean logDragon = false;
    @Option("log-explosion-type.wither")
    public boolean logWither = false;
    @Option("log-explosion-type.ghast-fireball")
    public boolean logFireball = false;

    @Override
    public String getName()
    {
        return "block-explode";
    }
}
