package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.rulebook.bookManagement.RuleBookCommands;
import de.cubeisland.cubeengine.rulebook.bookManagement.RuleBookManager;

public class Rulebook extends Module
{
    private RuleBookConfiguration config;
    private RuleBookManager ruleBookManager;
    
    @Override
    public void onEnable()
    {
        this.getFileManager().dropResources(RuleBookResource.values());

        this.ruleBookManager = new RuleBookManager(this);
        
        this.registerCommands(new RuleBookCommands(this), "rulebook");
        this.registerListener(new RuleBookListener(this));
    }

    public RuleBookConfiguration getConfig()
    {
        return this.config;
    }
    
    public RuleBookManager getRuleBookManager()
    {
        return this.ruleBookManager;
    }
}
