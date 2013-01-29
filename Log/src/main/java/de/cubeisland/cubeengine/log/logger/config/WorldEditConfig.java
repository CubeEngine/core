package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.log.SubLogConfig;

public class WorldEditConfig extends SubLogConfig{

    public WorldEditConfig() {
        super(false);
    }

    @Override
    public String getName() {
        return "worldedit";
    }
}
