package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import java.util.logging.Level;

/**
 * Main Class
 */
public class Rulebook extends Module
{
    private RuleBookConfiguration config;

    @Override
    public void onEnable()
    {
        this.getFileManager().dropResources(RuleBookResource.values());

        this.config = new RuleBookConfiguration(this);
        this.registerListener(new RuleBookListener(this));
    }

    public void error(String msg, Throwable t)
    {
        this.getLogger().log(Level.SEVERE, msg, t);
    }

    public RuleBookConfiguration getConfig()
    {
        return this.config;
    }
}
