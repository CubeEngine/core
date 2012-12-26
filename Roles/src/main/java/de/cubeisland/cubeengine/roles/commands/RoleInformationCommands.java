package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import org.bukkit.World;

public class RoleInformationCommands extends RoleCommandHelper
{
    public RoleInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles [in world]",
             usage = "[in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 1)
    public void list(CommandContext context)
    {
        boolean global = !context.hasNamed("in");
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        if (provider.getRoles().isEmpty())
        {
            context.sendMessage("roles", global
                    ? "&eNo global roles found!"
                    : "&eNo roles found in &6%s&e!", world.getName());
        }
        else
        {
            context.sendMessage("roles", global
                    ? "&aThe following global roles are available!"
                    : "&aThe following roles are available in &6%s&a!",
                    global ? null : world.getName());
            for (Role role : provider.getRoles().values())
            {
                context.sendMessage(String.format(" - &6%s", role.getName()));
            }
        }
    }

    @Alias(names = "checkrperm")
    @Command(names =
    {
        "checkperm", "checkpermission"
    },
             desc = "Checks the permission in given role [in world]",
             usage = "<[g:]role> <permission> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void checkperm(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith("g:") ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String permission = context.getString(1);
        RolePermission myPerm = role.getPerms().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                context.sendMessage("roles", roleName.startsWith("g:")
                        ? "&6%s &ais set to &2true &ain the global role &6%s&a."
                        : "&6%s &ais set to &2true &ain the role &6%s &ain &6%s&a.",
                        context.getString(1), role.getName(), world.getName());
            }
            else
            {
                context.sendMessage("roles", "&6%s &cis set to &4false &cin the role &6%s &cin &6%s&c.",
                        context.getString(1), role.getName(), world.getName());
            }
        }
        else
        {
            context.sendMessage("roles",
                    "&eThe permission &6%s &eis not assinged in the role &6%s &ein &6%s&e.",
                    context.getString(1), role.getName(), world.getName());
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
    @Command(names =
    {
        "listperm", "listpermission"
    },
             desc = "Lists all permissions of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listperm(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith("g:") ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getPerms().isEmpty())
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&eNo permissions set for the global role &6%s&e."
                    : "&eNo permissions set for the role &6%s &ein &6%s&e.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&aPermissions of the global role &6%s&a."
                    : "&aPermissions of the role &6%s &ain &6%s&a:",
                    role.getName(), world.getName());
            for (String perm : role.getLitaralPerms().keySet())
            {
                if (role.getLitaralPerms().get(perm))
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
    @Command(names =
    {
        "listdata", "listmeta", "listmetadata"
    },
             desc = "Lists all metadata of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listmetadata(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith("g:") ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getMetaData().isEmpty())
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&eNo metadata set for the global role &6%s&e."
                    : "&eNo metadata set for the role &6%s &ein &6%s&e.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&aMetadata of the global role &6%s&a."
                    : "&aMetadata of the role &6%s &ain &6%s&a:",
                    role.getName(), world.getName());
            for (RoleMetaData data : role.getMetaData().values())
            {
                context.sendMessage(" - " + data.getKey() + ": " + data.getValue());
            }
        }
    }

    @Command(
    desc = "Lists all parents of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listParent(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith("g:") ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getParentRoles().isEmpty())
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&eThe global role &6%s &ehas no parent roles."
                    : "&eThe role &6%s &ein &6%s &ehas no parent roles.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", roleName.startsWith("g:")
                    ? "&eThe global role &6%s &ehas following parent roles:"
                    : "&eThe role &6%s &ein &6%s &ehas following parent roles:",
                    role.getName(), world.getName());
            for (Role parent : role.getParentRoles())
            {
                context.sendMessage(" - " + parent.getName());
            }
        }
    }

    @Command(
            names =
    {
        "prio", "priotory"
    },
             desc = "Show the priority of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void priority(CommandContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith("g:") ? null : this.getWorld(context);
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
}
