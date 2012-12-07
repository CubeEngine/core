package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.MergedRole;
import de.cubeisland.cubeengine.roles.role.Role;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Collection;

public class UserManagementCommands extends ContainerCommand
{
    public UserManagementCommands(Roles module)
    {
        super(module, "user", "Manage users.");//TODO alias manuser
    }

    @Alias(names = "listuroles")
    @Command(desc = "Lists roles of a user [in world]",
             usage = "[player] [in <world>]",
             max = 2)
    public void list(CommandContext context)
    {
        User user;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
        }
        else
        {
            user = context.getSenderAsUser("roles", "&cYou have to specify a player.");
        }
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(0));
            return;
        }
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            throw new IllegalStateException("User has no rolecontainer!");
        }
        Integer worldId;
        if (context.hasIndexed(1))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(1));
            if (worldId == null)
            {
                context.sendMessage("roles", "&cUnkown world %s!", context.getString(1));
                return;
            }
        }
        else
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(user.getWorld());
        }
        MergedRole mergedRole = roleContainer.get(worldId);
        Collection<Role> roles = mergedRole.getMergedWith();
        String world = this.getModule().getCore().getWorldManager().getWorld(worldId).getName();
        context.sendMessage("roles", "&eRoles of &2%s&e in &6%s&e:", user.getName(), world);
        for (Role role : roles)
        {
            if (role.isGlobal())
            {
                context.sendMessage("&6global&e: " + role.getName());
            }
            else
            {
                context.sendMessage("&6" + world + "&e: " + role.getName());
            }
        }
    }

    @Alias(names = "checkuperm")
    @Command(desc = "Checks for permissions of a user [in world]",
             usage = "[player] [in <world>]",
             max = 2)
    public void checkperm(CommandContext context)
    {
        User user;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
        }
        else
        {
            user = context.getSenderAsUser("roles", "&cYou have to specify a player.");
        }
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(0));
            return;
        }
        TIntObjectHashMap<MergedRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            throw new IllegalStateException("User has no rolecontainer!");
        }
        Integer worldId;
        if (context.hasIndexed(1))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(1));
            if (worldId == null)
            {
                context.sendMessage("roles", "&cUnkown world %s!", context.getString(1));
                return;
            }
        }
        else
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(user.getWorld());
        }
        String permission = context.getString(2);
        MergedRole mergedRole = roleContainer.get(worldId);
        String permissionfound = null;
        while (permission.contains("."))
        {
            if (mergedRole.getPermissions().containsKey(permission))
            {
                permissionfound = permission;
            }
            if (permission.endsWith("*"))
            {
                permission = permission.substring(0, permission.lastIndexOf("."));
            }
            permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";
        }
        if (permissionfound == null)
        {
            //TODO perm not found
        }
        //TODO ...
    }

    public void listperm(CommandContext context)
    {
    }

    public void checkmetadata(CommandContext context)
    {
    }

    public void listmetadata(CommandContext context)
    {
    }

    public void assign(CommandContext context)
    {
    }

    public void remove(CommandContext context)
    {
    }

    public void clear(CommandContext context)
    {
    }

    public void setpermission(CommandContext context)
    {
    }

    public void resetpermission(CommandContext context)
    {
    }

    public void setmetadata(CommandContext context)
    {
    }

    public void resetmetadata(CommandContext context)
    {
    }
}
