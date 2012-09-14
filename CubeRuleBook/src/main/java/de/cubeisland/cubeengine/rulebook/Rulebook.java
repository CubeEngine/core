package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main Class
 */
public class Rulebook extends Module
{
    private RuleBookConfiguration config;
    private Logger logger;
    
    @Override
    public void onEnable()
    {
        this.logger = this.getLogger();
        CubeEngine.getFileManager().dropResources(RuleBookResource.values());
        
        this.config = new RuleBookConfiguration(this);
        this.getCore().getEventManager().registerListener(new RuleBookListener(this), this); 
    }
    
    @Override
    public void onDisable()
    {
        logger = null;
    }
    
    public void error(String msg, Throwable t)
    {
        logger.log(Level.SEVERE, msg, t);
    }
    
    public RuleBookConfiguration getConfig()
    {
        return this.config;
    }
}

