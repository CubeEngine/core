package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.module.Module;

public class RoleCommands extends ContainerCommand
{
    public RoleCommands(Module module)
    {
        super(module, "role", "Manages the roles", "roles");
        this.addChild(new RoleManagementCommands(module));
    }

    public void add(CommandContext context)
    {
        //Assigns a role to the specified player
    }

    public void remove(CommandContext context)
    {
        //Removes a role from the specified player
    }

    public void clear(CommandContext context)
    {
        //Clear all roles of the specified player
        //reset to default? OR separate rest cmd
    }

    public void list(CommandContext context)
    {
        //Lists the roles of the specified player
    }

    public void checkpermission(CommandContext context)
    {
        //Checks a permission and displays where the permission came from
    }

    public void listpermissions(CommandContext context)
    {
        //Lists all permissions of a player
    }
    
    /* GROUPMANAGER cmds:   

     Overriding Permissions

     /manuaddp: Add permission directly to the player.
     /<command> <player> <permission>
     * 
     /manudelp: Removes permission directly from the player.
     /<command> <player> <permission>

     User variables

     /manuaddv: Add, or replaces, a variable to a user (like prefix or suffix).
     /<command> <user> <variable> <value>
     * 
     /manudelv: Remove a variable from a user.
     /<command> <user> <variable>
     * 
     /manulistv: List variables a user has (like prefix or suffix).
     /<command> <user>
     * 
     /manucheckv: Verify a value of a variable of user, and where it comes from.
     /<command> <user> <variable>
*/

}
