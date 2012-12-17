package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
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
        int worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RoleProvider provider = Roles.getInstance().getManager().getProvider(worldId);
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

    public void checkperm(CommandContext context)
    {
    }

    public void listperm(CommandContext context)
    {
    }

    public void listmetadata(CommandContext context)
    {
    }

    public void listParent(CommandContext context)
    {
    }

    public void priority(CommandContext context)
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

    public void clearmetadata(CommandContext context)
    {
    }

    public void addParent(CommandContext context)
    {
    }

    public void removeParent(CommandContext context)
    {
    }

    public void clearParent(CommandContext context)
    {
    }

    public void setPriority(CommandContext context)
    {
    }

    public void rename(CommandContext context)
    {
    }
}
