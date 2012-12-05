package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class RoleCommands extends ContainerCommand
{
    public RoleCommands(Module module)
    {
        super(module, "role", "Manages the roles", "roles");
        this.addChild(new RoleManagementCommands(module));
        this.addChild(new UserManagementCommands(module));
        this.addChild(new ModuleManagementCommands(module));
    }
}
