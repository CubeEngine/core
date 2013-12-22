package de.cubeisland.engine.backpack;

import de.cubeisland.engine.core.module.Module;

public class Backpack extends Module
{
    private BackpackConfig config;
    
    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(BackpackConfig.class);
    }
}
