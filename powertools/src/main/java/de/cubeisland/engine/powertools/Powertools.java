package de.cubeisland.engine.powertools;

import de.cubeisland.engine.core.module.Module;

public class Powertools extends Module
{
    private PowertoolsPerm perm;
    
    @Override
    public void onEnable()
    {
        this.perm = new PowertoolsPerm(this);
        PowerToolCommand ptCommands = new PowerToolCommand(this);
        getCore().getCommandManager().registerCommand(ptCommands);
        getCore().getEventManager().registerListener(this, ptCommands);
    }
}
