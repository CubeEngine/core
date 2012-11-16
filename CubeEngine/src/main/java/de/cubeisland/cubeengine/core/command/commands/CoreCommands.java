package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.module.CoreModule;

public class CoreCommands extends ContainerCommand
{
    private final BukkitCore core;

    public CoreCommands(Core core)
    {
        super(CoreModule.get(), "cubeengine", "These are the basic commands of the CubeEngine.", "ce");
        this.core = (BukkitCore)core;
    }

    @Command(desc = "Disables the CubeEngine")
    public void disable(CommandContext context)
    {
        this.core.getServer().getPluginManager().disablePlugin(this.core);
    }
}
