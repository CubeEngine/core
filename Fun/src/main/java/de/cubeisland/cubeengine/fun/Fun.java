package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.*;

public class Fun extends Module
{
    private FunConfiguration config;

    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(FunResource.values());
        this.registerPermissions(FunPerm.values());

        this.registerCommands(new ThrowCommands(this), ReflectedCommand.class);
        this.registerCommands(new NukeCommand(this), ReflectedCommand.class);
        this.registerCommands(new PlayerCommands(this), ReflectedCommand.class);
        this.registerCommands(new DiscoCommand(this), ReflectedCommand.class);
        this.registerCommands(new InvasionCommand(this), ReflectedCommand.class);
        this.registerCommands(new RocketCommand(this), ReflectedCommand.class);
    }

    public FunConfiguration getConfig()
    {
        return this.config;
    }
}
