package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.log.LogLevel;

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
        this.getLogger().log(LogLevel.ERROR, msg, t);
    }

    public RuleBookConfiguration getConfig()
    {
        return this.config;
    }
}
