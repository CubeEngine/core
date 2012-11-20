package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.DiscoCommand;
import de.cubeisland.cubeengine.fun.commands.InvasionCommand;
import de.cubeisland.cubeengine.fun.commands.NukeCommand;
import de.cubeisland.cubeengine.fun.commands.PlayerCommands;
import de.cubeisland.cubeengine.fun.commands.RocketCommand;
import de.cubeisland.cubeengine.fun.commands.ThrowCommands;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;

public class Fun extends Module
{
    private FunConfiguration config;
    private NukeListener nukeListener;

    @Override
    public void onEnable()
    {
        this.nukeListener = new NukeListener();

        this.getCore().getFileManager().dropResources(FunResource.values());
        
        this.registerCommands(new ThrowCommands(this));
        this.registerCommands(new NukeCommand(this));
        this.registerCommands(new PlayerCommands(this));
        this.registerCommands(new DiscoCommand(this));
        this.registerCommands(new InvasionCommand(this));
        this.registerCommands(new RocketCommand(this));
        this.registerListener(this.nukeListener);
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
