package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

class RoleManagementCommands extends ContainerCommand
{
    public RoleManagementCommands(Module module)
    {
        super(module, "manage", "Creates, deletes or modifies roles.");
    }

    public void create(CommandContext context)
    {
    }

    public void delete(CommandContext context)
    {
    }

    public void addParent(CommandContext context)
    {
    }

    public void removeParent(CommandContext context)
    {
    }

    public void list(CommandContext context)
    {//list all roles
    }
    
    public void listPermission(CommandContext context)
    {
    }
    public void listMetaData(CommandContext context)
    {
    }        
}
