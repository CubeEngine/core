package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class ModuleManagementCommands extends ContainerCommand
{
    public ModuleManagementCommands(Module module)
    {
        super(module, "admin", "Manages the module.");

    }

    public void reload(CommandContext context)
    {
    }

    public void save(CommandContext context)
    {
    }

    public void defaultworld(CommandContext context)
    {
    }
}
