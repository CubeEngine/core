package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.NukeCommand;
import de.cubeisland.cubeengine.fun.commands.PlayerCommands;
import de.cubeisland.cubeengine.fun.commands.ThrowCommands;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;

public class Fun extends Module
{
    private FunConfiguration config;
    private RocketListener rocketListener;
    private NukeListener nukeListener;

    @Override
    public void onEnable()
    {
//        this.rocketListener = new RocketListener(this);
        this.nukeListener = new NukeListener();

        this.getCore().getFileManager().dropResources(FunResource.values());
        
        this.registerCommands(new ThrowCommands(this));
        this.registerCommands(new NukeCommand(this));
        this.registerCommands(new PlayerCommands(this));
//        this.registerListener(this.rocketListener);
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
