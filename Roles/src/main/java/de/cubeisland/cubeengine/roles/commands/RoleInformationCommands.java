package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
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
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        if (provider.getAllRoles().isEmpty())
        {
            context.sendMessage("roles", "&eNo roles found in &6%s&e!", world.getName());
        }
        else
        {
            context.sendMessage("roles", "&aThe following roles are available in &6%s&a!", world.getName());
            for (Role role : provider.getAllRoles())
            {
                context.sendMessage(String.format(" - &6%s", role.getName()));
            }
        }
    }

    @Alias(names = "checkperm")
    @Command(names =
    {
        "checkperm", "checkpermission"
    },
             desc = "Checks the permission in given role [in world]",
             usage = "<role> <permission> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void checkperm(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        String permission = context.getString(1);
        RolePermission myPerm = role.getPerms().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                context.sendMessage("roles", "&6%s &ais set to &2true &ain the role &6%s &ain &6%s&a.",
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

    @Alias(names = "listperm")
    @Command(names =
    {
        "listperm", "listpermission"
    },
             desc = "Lists all permissions of given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listperm(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        if (role.getPerms().isEmpty())
        {
            context.sendMessage("roles", "&eNo permissions set in the role &6%s &ein &6%s&e.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&aPermissions of the role &6%s &ain &6%s&a:",
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

    @Alias(names = "listdata")
    @Command(names =
    {
        "listdata", "listmetadata"
    },
             desc = "Lists all metadata of given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listmetadata(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        if (role.getMetaData().isEmpty())
        {
            context.sendMessage("roles", "&eNo metadata set in the role &6%s &ein &6%s&e.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&aMetadata of the role &6%s &ain &6%s&a:",
                    role.getName(), world.getName());
            for (RoleMetaData data : role.getMetaData().values())
            {
                context.sendMessage(" - " + data.getKey() + ": " + data.getValue());
            }
        }
    }

    @Command(
    desc = "Lists all parents of given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void listParent(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        if (role.getParentRoles().isEmpty())
        {
            context.sendMessage("roles", "&eThe role &6%s &ein &6%s &ehas no parent roles.",
                    role.getName(), world.getName());
        }
        else
        {
            context.sendMessage("roles", "&eThe role &6%s &ein &6%s &ehas following parent roles.",
                    role.getName(), world.getName());
            for (Role parent : role.getParentRoles())
            {
                context.sendMessage(" - " + parent.getName());
            }
        }
    }

    @Command(
    desc = "Show the priority of given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void priority(CommandContext context)
    {
        World world = this.getWorld(context);
        RoleProvider provider = this.getProvider(world);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        context.sendMessage("roles", "&eThe priority of the role &6%s &ein &6%s &eis: &6%d", role.getName(), world.getName(), role.getPriority().value);
    }
}
