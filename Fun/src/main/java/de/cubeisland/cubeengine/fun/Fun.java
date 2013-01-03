package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.DiscoCommand;
import de.cubeisland.cubeengine.fun.commands.InvasionCommand;
import de.cubeisland.cubeengine.fun.commands.NukeCommand;
import de.cubeisland.cubeengine.fun.commands.PlayerCommands;
import de.cubeisland.cubeengine.fun.commands.RocketCommand;
import de.cubeisland.cubeengine.fun.commands.ThrowCommands;

public class Fun extends Module
{
    private FunConfiguration config;

    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(FunResource.values());
        this.registerPermissions(FunPerm.values());

        this.registerCommands(new ThrowCommands(this));
        this.registerCommands(new NukeCommand(this));
        this.registerCommands(new PlayerCommands(this));
        this.registerCommands(new DiscoCommand(this));
        this.registerCommands(new InvasionCommand(this));
        this.registerCommands(new RocketCommand(this));
    }

    public FunConfiguration getConfig()
    {
        return this.config;
    }
}
