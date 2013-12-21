package de.cubeisland.engine.portals;

import de.cubeisland.engine.core.module.Module;

public class Portals extends Module
{
    private PortalsConfig config;
    
    @Override
    public void onEnable()
    {
        this.config = Configuration.load(PortalsConfig.class, this);
    }
}
