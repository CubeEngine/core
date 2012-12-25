package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import java.util.Set;
import org.bukkit.World;

public class UserManagementCommands extends UserCommandHelper
{
    public UserManagementCommands(Roles module)
    {
        super(module);
    }

    @Alias(names =
    {
        "manuadd", "assignurole", "addurole", "giveurole"
    })
    @Command(names =
    {
        "assign", "add", "give"
    },
             desc = "Assign a role to the player [in world]",
             usage = "<role> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void assign(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        String roleName = context.getString(0);
        Role role = this.manager.getProvider(worldId).getRole(roleName);
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", roleName, world.getName());
            return;
        }
        if (this.manager.addRoles(user, user.getPlayer(), worldId, role))
        {
            context.sendMessage("roles", "&aAdded the role &6%s&a to &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e already had the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
        }
    }

    @Alias(names =
    {
        "remurole", "manudel"
    })
    @Command(desc = "Removes a role from the player [in world]",
             usage = "<role> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void remove(CommandContext context)
    {
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.manager.getProvider(worldId).getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s &ein &6%s&e.", context.getString(0), world.getName());
            return;
        }
        if (this.manager.removeRole(user, role, worldId))
        {
            context.sendMessage("roles", "&aRemoved the role &6%s&a from &2%s&a in &6%s&a.", role.getName(), user.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&2%s&e did not have the role &6%s&e in &6%s&e.", user.getName(), role.getName(), world.getName());
        }
    }

    @Alias(names = "clearurole")
    @Command(desc = "Clears all roles from the player and sets the defaultroles [in world]",
             usage = "<player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clear(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Set<Role> newRoles = this.manager.clearRoles(user, worldId);
        context.sendMessage("roles", "&eCleared the roles of &2%s &ein &6%s&e.", user.getName(), world.getName());
        if (!newRoles.isEmpty())
        {
            context.sendMessage("roles", "&eDefault roles assigned:");
            for (Role role : newRoles)
            {
                context.sendMessage("- &6" + role.getName());
            }
        }
    }

    @Command(names =
    {
        "setperm", "setpermission"
    },
             desc = "Sets a permission for this user [in world]",
             usage = "<permission> <true|false|reset> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 5, min = 3)
    public void setpermission(CommandContext context)
    {
        User user = this.getUser(context, 2);
        String perm = context.getString(0);
        Boolean set;
        String setTo = context.getString(1);
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
        }
        else
        {
            context.sendMessage("roles", "&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setPermission(perm, set);
        if (set == null)
        {
            context.sendMessage("roles", "&ePermission &6%s &eof &2%s&e resetted!", perm, user.getName());
        }
        else
        {
            if (set)
            {
                context.sendMessage("roles", "&aPermission &6%s &aof &2%s&a set to true!", perm, user.getName());
            }
            else
            {
                context.sendMessage("roles", "&cPermission &6%s &cof &2%s&c set to false!", perm, user.getName());
            }
        }
    }

    public void resetpermission(CommandContext context)
    {
        //TODO use this as proxy method for setPermission with reset
        //other alias givePermissions for setPermission with true
    }

    @Command(names =
    {
        "setdata", "setmeta", "setmetadata"
    },
             desc = "Sets metadata for this user [in world]",
             usage = "<metaKey> <metaValue> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        String metaVal = context.getString(1);
        User user = this.getUser(context, 2);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, metaVal);
        context.sendMessage("roles", "&aMetadata &6%s &aof &2%s&a set to &6%s &ain &6%s&a!", metaKey, user.getName(), metaVal, world.getName());
    }

    @Command(names =
    {
        "resetdata", "resetmeta", "resetmetadata"
    },
             desc = "Resets metadata for this user [in world]",
             usage = "<metaKey> <player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void resetmetadata(CommandContext context)
    {
        String metaKey = context.getString(0);
        User user = this.getUser(context, 1);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, null);
        context.sendMessage("roles", "&eMetadata &6%s &eof &2%s &eremoved in &6%s&e!", metaKey, user.getName(), world.getName());
    }

    @Command(names =
    {
        "cleardata", "clearmeta", "clearmetadata"
    },
             desc = "Resets metadata for this user [in world]",
             usage = "<player> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearMetaData(CommandContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context, user);
        UserSpecificRole role = this.getUserRole(user, world);
        role.clearMetaData();
        context.sendMessage("roles", "&eMetadata of &2%s &ecleared in &6%s&e!", user.getName(), world.getName());
    }
}
