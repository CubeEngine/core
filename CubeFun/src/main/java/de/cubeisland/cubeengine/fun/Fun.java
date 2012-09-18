package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class Fun extends Module
{
    private Logger logger;
    private FunConfiguration config;
    
    @Override
    public void onEnable()
    {
        this.logger = this.getLogger();
        CubeEngine.getFileManager().dropResources(FunResource.values());
        
        this.config = Configuration.load(FunConfiguration.class, this);
    }
}
