package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import org.bukkit.World;

public class RoleManagementCommands extends RoleCommandHelper
{
    public RoleManagementCommands(Roles module)
    {
        super(module);
    }

    @Command(
            names =
    {
        "setperm", "setpermission"
    },
             desc = "Sets the permission for given role [in world]",
             usage = "<role> <permission> <true|false|reset> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setpermission(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        String permission = context.getString(1);
        Boolean set;
        String setTo = context.getString(2);
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
            context.sendMessage("roles", "&6%s &ais now set to &2true &afor the role &6%s &ain &6%s&a!", permission, role.getName(), world.getName());
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
            context.sendMessage("roles", "&6%s &cis now set to &4false &cfor the role &6%s &cin &6%s&c!", permission, role.getName(), world.getName());
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
            context.sendMessage("roles", "&6%s &eis now resetted for the role &6%s &ein &6%s&e!", permission, role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        provider.setRolePermission(role, permission, set);
    }

    public void resetpermission(CommandContext context)
    {
        //same as setpermission with reset as 3rd param
    }

    @Command(
            names =
    {
        "setdata", "setmeta", "setmetadata"
    },
             desc = "Sets the metadata for given role [in world]",
             usage = "<role> <key> [value] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setmetadata(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        String key = context.getString(1);
        String value = context.getString(2);
        provider.setRoleMetaData(role, key, value);
        if (value == null)
        {
            context.sendMessage("roles", "&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!", key, role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&aMetadata &6%s &aset to &6%s &afor the role &6%s &ain &6%s&a!", key, value, role.getName(), world.getName());
        }
    }

    @Command(
            names =
    {
        "resetdata", "resetmeta", "resetmetadata"
    },
             desc = "Resets the metadata for given role [in world]",
             usage = "<role> <key> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void resetmetadata(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        String key = context.getString(1);
        provider.resetRoleMetaData(role, key);
        context.sendMessage("roles", "&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!", key, role.getName(), world.getName());
    }

    @Command(
            names =
    {
        "cleardata", "clearmeta", "clearmetadata"
    },
             desc = "Clears the metadata for given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearmetadata(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        provider.clearRoleMetaData(role);
        context.sendMessage("roles", "&eMetadata cleared for the role &6%s &ein &6%s&e!", role.getName(), world.getName());
    }

    @Command(
    desc = "Adds a parent role to given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void addParent(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            context.sendMessage("roles", "&eCould not find the parent-role &6%s&e.", context.getString(1));
            return;
        }
        try
        {
            if (provider.setParentRole(role, pRole))
            {
                context.sendMessage("roles", "&aAdded &6%s &aas parent-role for &6%s&a!", pRole.getName(), role.getName());
            }
            else
            {
                context.sendMessage("roles", "&6%s &eis already parent-role of &6%s&a!", pRole.getName(), role.getName());
            }
        }
        catch (CircularRoleDepedencyException ex)
        {
            context.sendMessage("roles", "&cCircular Dependency! &6%s &cdepends on &6%s&c!", pRole.getName(), role.getName());
        }
    }

    @Command(
    desc = "Removes a parent role from given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void removeParent(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            context.sendMessage("roles", "&eCould not find the parent-role &6%s&e.", context.getString(1));
            return;
        }
        if (provider.removeParentRole(role, pRole))
        {
            context.sendMessage("roles", "&aRemoved the parent-role &6%s &afrom &6%s&a!", pRole.getName(), role.getName());
        }
        else
        {
            context.sendMessage("roles", "&6%s &eis not a parent-role of &6%s&e!", pRole.getName(), role.getName());
        }
    }

    @Command(
    desc = "Removes all parent roles from given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearParent(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        provider.clearParentRoles(role);
        context.sendMessage("roles", "&eAll parent-roles of &6%s &ecleared!", role.getName());
    }

    @Command(
            names =
    {
        "setprio", "setPriority"
    },
             desc = "Sets the priority of given role [in world]",
             usage = "<[g:]role> <priority> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void setPriority(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        Converter<Priority> converter = Convert.matchConverter(Priority.class);
        Priority priority;
        try
        {
            priority = converter.fromObject(context.getString(1));
        }
        catch (ConversionException ex)
        {
            context.sendMessage("roles", "&6%s &cis not a valid priority!", context.getString(1));
            return;
        }
        provider.setRolePriority(role, priority);
        context.sendMessage("roles", "&aPriority of &6%s &aset to &6%s &ain &6%s&a!", role.getName(), context.getString(1), world.getName());
    }

    @Command(
    desc = "Renames given role [in world]",
             usage = "<[g:]role> <new name> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void rename(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = this.getRole(context, provider, context.getString(0), world);
        String newName = context.getString(1);
        if (role.getName().equalsIgnoreCase(newName))
        {
            context.sendMessage("roles", "&cThese are the same names!");
            return;
        }
        if (provider.renameRole(role, newName))
        {
            context.sendMessage("roles", "&6%s &arenamed to &6%s &ain &6%s&a!", role.getName(), newName, world.getName());
        }
        else
        {
            context.sendMessage("roles", "&cRenaming failed! The role &6%s &calready exists in &6%s&c!", newName, world.getName());
        }
    }

    @Command(
    desc = "Creates a new role [in world]",
             usage = "<rolename> [in <world>] [-global]",
             params =
    @Param(names = "in", type = World.class),
             flags =
    @Flag(longName = "global", name = "g"),
             max = 2, min = 1)
    public void create(CommandContext context)
    {
        String roleName = context.getString(0);
        if (context.hasFlag("g"))
        {
            if (this.manager.createGlobalRole(roleName))
            {
                context.sendMessage("roles", "&aGlobal role created!");
            }
            else
            {
                context.sendMessage("roles", "&eThere is already a global role named &6%s&e.", roleName);
            }
        }
        else
        {
            World world = this.getWorld(context);
            RoleProvider provider = this.getProvider(world);
            if (provider.createRole(roleName))
            {
                context.sendMessage("roles", "&aRole created!");
            }
            else
            {
                context.sendMessage("roles", "&eThere is already a role named &6%s&e.", roleName);
            }
        }
    }
}
