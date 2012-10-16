package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.FunCommands;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;

public class Fun extends Module
{
    @From
    private FunConfiguration config;
    
    private RocketListener rocketListener;
    private NukeListener nukeListener;

    @Override
    public void onEnable()
    {
        this.rocketListener = new RocketListener(this);
        this.nukeListener = new NukeListener();

        this.getCore().getFileManager().dropResources(FunResource.values());
        this.registerCommands(new FunCommands(this));
        this.registerListener(this.rocketListener);
        this.registerListener(this.nukeListener);
    }

    public RocketListener getRocketListener()
    {
        return this.rocketListener;
    }
    
    public NukeListener getNukeListener()
    {
        return this.nukeListener;
    }
    
    public FunConfiguration getConfig()
    {
        return this.config;
    }
}
