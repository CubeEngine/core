package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fun.commands.*;

public class Fun extends Module
{
    private FunConfiguration config;
    private FunPerm perm;

    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(FunResource.values());
        this.perm = new FunPerm(this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommands(this, new ThrowCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new NukeCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new PlayerCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new DiscoCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new InvasionCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new RocketCommand(this), ReflectedCommand.class);
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
    }

    public FunConfiguration getConfig()
    {
        return this.config;
    }
}
