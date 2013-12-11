package de.cubeisland.engine.multiverse;

import de.cubeisland.engine.core.module.Module;

public class Multiverse extends Module
{
    private MultiverseConfig config;
    
    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(MultiverseConfig.class);
    }
}
