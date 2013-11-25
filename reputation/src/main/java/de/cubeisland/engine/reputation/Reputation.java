package de.cubeisland.engine.reputation;

import de.cubeisland.engine.core.module.Module;

public class Reputation extends Module
{
    private ReputationConfig config;
    
    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(ReputationConfig.class);
    }
}
