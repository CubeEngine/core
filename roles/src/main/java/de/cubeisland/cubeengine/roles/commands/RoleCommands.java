package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.roles.Roles;

public class RoleCommands extends ContainerCommand
{
    public RoleCommands(Roles module)
    {
        super(module, "roles", "Manages the roles");
    }
}
