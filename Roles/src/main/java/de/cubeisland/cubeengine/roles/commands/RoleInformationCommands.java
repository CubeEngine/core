package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import de.cubeisland.cubeengine.roles.role.WorldRoleProvider;
import java.util.Set;
import org.bukkit.World;

public class RoleInformationCommands extends RoleCommandHelper
{
    public RoleInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles [in world]|[-global]", usage = "[in <world>]", params = @Param(names = "in", type = World.class), flags = @Flag(longName = "global", name = "g"), max = 1)
    public void list(CommandContext context)
    {
        boolean global = context.hasFlag("g");
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        if (provider.getRoles().isEmpty())
        {
            if (global)
            {
                context.sendMessage("roles", "&eNo global roles found!");
            }
            else
            {
                context.sendMessage("roles", "&eNo roles found in &6%s&e!", world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendMessage("roles", "&aThe following global roles are available:");
            }
            else
            {
                context.sendMessage("roles", "&aThe following roles are available in &6%s&a:", world.getName());
            }
            for (Role role : provider.getRoles().values())
            {
                context.sendMessage(String.format(" - &6%s", role.getName()));
            }
        }
    }

    @Alias(names = "checkrperm")
    @Command(names = {
        "checkperm", "checkpermission"
    }, desc = "Checks the permission in given role [in world]", usage = "<[g:]role> <permission> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void checkperm(CommandContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String permission = context.getString(1);
        RolePermission myPerm = role.getPerms().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                if (global)
                {
                    context.sendMessage("roles", "&6%s &ais set to &2true &afor the global role &6%s&a.",
                            context.getString(1), role.getName());
                }
                else
                {
                    context.sendMessage("roles", "&6%s &ais set to &2true &afor the role &6%s &ain &6%s&a.",
                            context.getString(1), role.getName(), world.getName());
                }
            }
            else
            {
                if (global)
                {
                    context.sendMessage("roles", "&6%s &cis set to &4false &cfor the global role &6%s&c.",
                            context.getString(1), role.getName());
                }
                else
                {
                    context.sendMessage("roles", "&6%s &cis set to &4false &cfor the role &6%s &cin &6%s&c.",
                            context.getString(1), role.getName(), world.getName());
                }
            }
        }
        else
        {
            if (global)
            {
                context.sendMessage("roles", "&eThe permission &6%s &eis not assinged in the global role &6%s&e.",
                        context.getString(1), role.getName());
            }
            else
            {
                context.sendMessage("roles", "&eThe permission &6%s &eis not assinged in the role &6%s &ein &6%s&e.",
                        context.getString(1), role.getName(), world.getName());
            }
        }
        Role originRole = myPerm.getOrigin();
        if (!originRole.getLitaralPerms().containsKey(permission))
        {
            boolean found = false;
            while (!permission.equals("*"))
            {
                if (permission.endsWith("*"))
                {
                    permission = permission.substring(0, permission.lastIndexOf("."));
                }
                permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";

                if (originRole.getLitaralPerms().containsKey(permission))
                {
                    if (originRole.getLitaralPerms().get(permission) == myPerm.isSet())
                    {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
            {
                throw new IllegalStateException("Found permission not found in literal permissions");
            }
        }
        context.sendMessage("roles", "&ePermission inherited from:");
        context.sendMessage("roles", "&6%s &ein the role &6%s&e!", permission, originRole.getName());
    }

    @Alias(names = "listrperm")
    @Command(names = {
        "listperm", "listpermission"
    }, desc = "Lists all permissions of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listperm(CommandContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getPerms().isEmpty())
        {
            if (global)
            {
                context.sendMessage("roles", "&eNo permissions set for the global role &6%s&e.", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&eNo permissions set for the role &6%s &ein &6%s&e.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendMessage("roles", "&aPermissions of the global role &6%s&a.", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&aPermissions of the role &6%s &ain &6%s&a:", role.getName(), world.getName());
            }
            for (String perm : role.getAllLiteralPerms().keySet())
            {
                if (role.getAllLiteralPerms().get(perm))
                {
                    context.sendMessage(" - &6" + perm + "&f: &2true");
                }
                else
                {
                    context.sendMessage(" - &6" + perm + "&f: &4false");
                }
            }
        }
    }

    @Alias(names = "listrdata")
    @Command(names = {
        "listdata", "listmeta", "listmetadata"
    }, desc = "Lists all metadata of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listmetadata(CommandContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getMetaData().isEmpty())
        {
            if (global)
            {
                context.sendMessage("roles", "&eNo metadata set for the global role &6%s&e.", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&eNo metadata set for the role &6%s &ein &6%s&e.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendMessage("roles", "&aMetadata of the global role &6%s&a:", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&aMetadata of the role &6%s &ain &6%s&a:", role.getName(), world.getName());
            }
            for (RoleMetaData data : role.getMetaData().values())
            {
                context.sendMessage(" - " + data.getKey() + ": " + data.getValue());
            }
        }
    }

    @Command(desc = "Lists all parents of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listParent(CommandContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getParentRoles().isEmpty())
        {
            if (global)
            {
                context.sendMessage("roles", "&eThe global role &6%s &ehas no parent roles.", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&eThe role &6%s &ein &6%s &ehas no parent roles.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendMessage("roles", "&eThe global role &6%s &ehas following parent roles:", role.getName());
            }
            else
            {
                context.sendMessage("roles", "&eThe role &6%s &ein &6%s &ehas following parent roles:", role.getName(), world.getName());
            }
            for (Role parent : role.getParentRoles())
            {
                context.sendMessage(" - " + parent.getName());
            }
        }
    }

    @Command(names = {
        "prio", "priotory"
    }, desc = "Show the priority of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void priority(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith(GLOBAL_PREFIX) ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (world == null)
        {
            context.sendMessage("roles", "&eThe priority of the global role &6%s &eis: &6%d", role.getName(), role.getPriority().value);
        }
        else
        {
            context.sendMessage("roles", "&eThe priority of the role &6%s &ein &6%s &eis: &6%d", role.getName(), world.getName(), role.getPriority().value);
        }
    }

    @Command(names = {
        "listdefault", "listdefroles", "listdefaultroles"
    }, desc = "Lists all default roles [in world]", usage = "[in <world>]", params = @Param(names = "in", type = World.class), max = 1)
    public void listDefaultRoles(CommandContext context)
    {
        World world = this.getWorld(context);
        WorldRoleProvider provider = this.manager.getProvider(world);
        Set<Role> defaultRoles = provider.getDefaultRoles();
        if (defaultRoles.isEmpty())
        {
            context.sendMessage("roles", "&cThere are no default roles set for &6%s&c!", world.getName());
        }
        else
        {
            context.sendMessage("roles", "&aThe following roles are default roles in &6%s&a!", world.getName());
            for (Role role : defaultRoles)
            {
                context.sendMessage(" - &6" + role.getName());
            }
        }
    }
}
