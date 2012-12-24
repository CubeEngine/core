package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.RolePermission;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import org.bukkit.World;

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
            //TODO msg define true|false|reset
            return;
        }
        ((Roles) this.getModule()).getManager().getProvider(worldId).setRolePermission(role, permission, set);
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
        context.sendMessage("roles", "&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!", key, role.getName(), world.getName());
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
        if (provider.setParentRole(role, pRole))
        {
            context.sendMessage("roles", "&aAdded &6%s &aas parent-role for &6%s&a!", pRole.getName(), role.getName());
        }
        else
        {
            context.sendMessage("roles", "&6%s &eis already parent-role of &6%s&a!", pRole.getName(), role.getName());
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
        if (provider.removeParentRole(role, pRole))
        {
            context.sendMessage("roles", "&aRemoved the parent-role &6%s &afrom &6%s&a!", pRole.getName(), role.getName());
        }
        else
        {
            context.sendMessage("roles", "&6%s &eis not a parent-role of &6%s&e!", pRole.getName(), role.getName());
        }
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
        context.sendMessage("roles", "&eAll parent-roles of &6%s &ecleared!", role.getName());
    }

    @Command(
    desc = "Sets the priority of given role [in world]",
             usage = "<[g:]role> <priority> [in <world>]",
             params =
    @Param(names = "in", type = World.class),
             max = 3, min = 2)
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
        if (role.getName().equalsIgnoreCase(newName))
        {
            context.sendMessage("roles", "&cThese are the same names!");
            return;
        }
        if (provider.renameRole(role, newName))
        {
            context.sendMessage("roles", "&6%s &arenamed to &6%s &ain &&%s&a!", role.getName(), newName, world.getName());
        }
        else
        {
            context.sendMessage("roles", "&cRenaming failed! The role &6%s &calready exists in &6%s&c!", newName, world.getName());
        }
    }
}
