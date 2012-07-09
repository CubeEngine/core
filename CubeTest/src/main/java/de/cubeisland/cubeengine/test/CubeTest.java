package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
        this.getLogger().info("Test1 onEnable...");
        Configuration.load(TestConfig.class, this);
    }

    @Override
    public void onDisable()
    {
    }
}
