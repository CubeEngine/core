package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.LoggerConfig;

public class WorldEditConfig extends LoggerConfig
{

    public WorldEditConfig()
    {
        super(false);
    }

    @Override
    public String getName()
    {
        return "worldedit";
    }
}
