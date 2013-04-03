package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import org.bukkit.World;

import java.util.Set;

public class UserManagementCommands extends UserCommandHelper
{
    public UserManagementCommands(Roles module)
    {
        super(module);
        this.registerAlias(new String[]{"manuser"},new String[]{});
    }

    @Alias(names = {
        "manuadd", "assignurole", "addurole", "giveurole"
    })
    @Command(names = {
        "assign", "add", "give"
    }, desc = "Assign a role to the player [in world]", usage = "<player> <role> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void assign(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        String roleName = context.getString(1);
        Role role = this.manager.getRoleInWorld(worldId,roleName);
        if (role == null)
        {
            context.sendTranslated("&eCould not find the role &6%s &ein &6%s&e.", roleName, world.getName());
            return;
        }
        if (role instanceof ConfigRole)
        {
            if (!((ConfigRole)role).canAssignAndRemove(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to assign the role &6%s&c in &6%s&c!",role.getName(),world.getName());
                return;
            }
        }
        else
        {
            throw new IllegalArgumentException("The role is not a ConfigRole!");
        }
        if (this.manager.addRoles(user, user.getPlayer(), worldId, role))
        {
            context.sendTranslated("&aAdded the role &6%s&a to &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
        }
        else
        {
            context.sendTranslated("&2%s&e already had the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
        }
    }

    @Alias(names = {
        "remurole", "manudel"
    })
    @Command(desc = "Removes a role from the player [in world]", usage = "<player> <role> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void remove(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.manager.getRoleInWorld(worldId,context.getString(1));
        if (role == null)
        {
            context.sendTranslated("&eCould not find the role &6%s &ein &6%s&e.", context.getString(0), world.getName());
            return;
        }
        if (role instanceof ConfigRole)
        {
            if (!((ConfigRole)role).canAssignAndRemove(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to remove the role &6%s&c in &6%s&c!",role.getName(),world.getName());
                return;
            }
        }
        else
        {
            throw new IllegalArgumentException("The role is not a ConfigRole!");
        }
        if (this.manager.removeRole(user, role, worldId))
        {
            context.sendTranslated("&aRemoved the role &6%s&a from &2%s&a in &6%s&a.", role.getName(), user.getName(), world.getName());
        }
        else
        {
            context.sendTranslated("&2%s&e did not have the role &6%s&e in &6%s&e.", user.getName(), role.getName(), world.getName());
        }
    }

    @Alias(names = "clearurole")
    @Command(desc = "Clears all roles from the player and sets the defaultroles [in world]", usage = "<player> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void clear(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Set<Role> newRoles = this.manager.clearRoles(user, worldId);
        context.sendTranslated("&eCleared the roles of &2%s &ein &6%s&e.", user.getName(), world.getName());
        if (!newRoles.isEmpty())
        {
            context.sendTranslated("&eDefault roles assigned:");
            for (Role role : newRoles)
            {
                context.sendMessage("- &6" + role.getName());
            }
        }
    }

    @Command(names = {
        "setperm", "setpermission"
    }, desc = "Sets a permission for this user [in world]", usage = " <player> <permission> <true|false|reset>[in <world>]", params = @Param(names = "in", type = World.class), max = 5, min = 3)
    public void setpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        String perm = context.getString(1);
        Boolean set;
        String setTo = context.getString(2);
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
            context.sendTranslated("&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        World world = this.getWorld(context);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setPermission(perm, set);
        if (set == null)
        {
            context.sendTranslated("&ePermission &6%s &eof &2%s&e resetted!", perm, user.getName());
        }
        else
        {
            if (set)
            {
                context.sendTranslated("&aPermission &6%s &aof &2%s&a set to true!", perm, user.getName());
            }
            else
            {
                context.sendTranslated("&cPermission &6%s &cof &2%s&c set to false!", perm, user.getName());
            }
        }
    }

    public void resetpermission(CommandContext context)
    {
    //TODO use this as proxy method for setPermission with reset
    //other alias givePermissions for setPermission with true
    }

    @Command(names = {
        "setdata", "setmeta", "setmetadata"
    }, desc = "Sets metadata for this user [in world]", usage = "<player> <metaKey> <metaValue> [in <world>]", params = @Param(names = "in", type = World.class), max = 4, min = 3)
    public void setmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        String metaVal = context.getString(2);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, metaVal);
        context.sendTranslated("&aMetadata &6%s &aof &2%s&a set to &6%s &ain &6%s&a!", metaKey, user.getName(), metaVal, world.getName());
    }

    @Command(names = {
        "resetdata", "resetmeta", "resetmetadata", "deletedata", "deletemetadata", "deletemeta"
    }, desc = "Resets metadata for this user [in world]", usage = "<player> <metaKey> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void resetmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        UserSpecificRole role = this.getUserRole(user, world);
        role.setMetaData(metaKey, null);
        context.sendTranslated("&eMetadata &6%s &eof &2%s &eremoved in &6%s&e!", metaKey, user.getName(), world.getName());
    }

    @Command(names = {
        "cleardata", "clearmeta", "clearmetadata"
    }, desc = "Resets metadata for this user [in world]", usage = "<player> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void clearMetaData(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        UserSpecificRole role = this.getUserRole(user, world);
        role.clearMetaData();
        context.sendTranslated("&eMetadata of &2%s &ecleared in &6%s&e!", user.getName(), world.getName());
    }
}
