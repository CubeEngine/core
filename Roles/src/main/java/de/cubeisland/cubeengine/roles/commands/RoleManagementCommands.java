package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import org.bukkit.World;
//TODO detect g: roles as global roles
public class RoleManagementCommands extends ContainerCommand
{
    public RoleManagementCommands(Roles module)
    {
        super(module, "role", "Manage roles.");//TODO alias manrole
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles [in world]",
             usage = "[in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 1)
    public void list(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
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
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        if (role.getPerms().containsKey(context.getString(1)))
        {
            Boolean isSet = role.getPerms().get(context.getString(1)).isSet();
            if (isSet)
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
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
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
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
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
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
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
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        context.sendMessage("roles", "&eThe priority of the role &6%s &ein &6%s &eis: &6%d", role.getName(), world.getName(), role.getPriority().value);
    }

    @Command(
    desc = "Sets the permission for given role [in world]",
             usage = "<role> <permission> <true|false|reset> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setpermission(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        String permission = context.getString(1);
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
            //TODO msg define true|false|reset
            return;
        }
        ((Roles) this.getModule()).getManager().getProvider(worldId).setRolePermission(role, permission, set);
        //TODO msg
    }

    public void resetpermission(CommandContext context)
    {
        //same as setpermission with reset as 3rd param
    }

    @Command(
    desc = "Sets the metadata for given role [in world]",
             usage = "<role> <key> [value] [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 4, min = 3)
    public void setmetadata(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        String key = context.getString(1);
        String value = context.getString(2);
        ((Roles) this.getModule()).getManager().getProvider(worldId).setRoleMetaData(role, key, value);
        //TODO msg
    }

    @Command(
    desc = "Resets the metadata for given role [in world]",
             usage = "<role> <key> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void resetmetadata(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        String key = context.getString(1);
        ((Roles) this.getModule()).getManager().getProvider(worldId).resetRoleMetaData(role, key);
        //TODO msg
    }

    @Command(
    desc = "Clears the metadata for given role [in world]",
             usage = "<role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearmetadata(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        ((Roles) this.getModule()).getManager().getProvider(worldId).clearRoleMetaData(role);
        //TODO msg
    }

    @Command(
    desc = "Adds a parent role to given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void addParent(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            context.sendMessage("roles", "&eCould not find the parent-role &6%s&e.", context.getString(1));
            return;
        }
        provider.setParentRole(role, pRole);
        //TODO msg
    }

    @Command(
    desc = "Removes a parent role from given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void removeParent(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            context.sendMessage("roles", "&eCould not find the parent-role &6%s&e.", context.getString(1));
            return;
        }
        provider.removeParentRole(role, pRole);
        //TODO msg
    }

    @Command(
    desc = "Removes all parent roles from given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void clearParent(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        provider.clearParentRoles(role);
    }

    @Command(
    desc = "Sets the priority of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void setPriority(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        Priority priority = null;//TODO set me
        provider.setRolePriority(role, priority);
    }

    @Command(
    desc = "Renames given role [in world]",
             usage = "<[g:]role> <new name> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 2, min = 1)
    public void rename(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                context.sendMessage("roles", "&ePlease provide a world.");//TODO show usage
                return;
            }
            world = sender.getWorld();
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                return;
            }
        }
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = ((Roles) this.getModule()).getManager().getProvider(worldId);
        Role role = provider.getRole(context.getString(0));
        if (role == null)
        {
            context.sendMessage("roles", "&eCould not find the role &6%s&e.", context.getString(0));
            return;
        }
        String newName = context.getString(1);
        provider.renameRole(role, newName);
    }
}
