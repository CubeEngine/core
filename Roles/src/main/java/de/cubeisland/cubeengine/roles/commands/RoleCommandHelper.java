package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import org.bukkit.World;

public abstract class RoleCommandHelper extends ContainerCommand
{
    protected static final String GLOBAL_PREFIX = "g:";
    protected RoleManager manager;
    protected Roles module;
    protected WorldManager worldManager;

    public RoleCommandHelper(Roles module)
    {
        super(module, "role", "Manage roles.");//TODO alias manrole
        this.manager = module.getManager();
        this.module = module;
        this.worldManager = module.getCore().getWorldManager();
    }

    protected World getWorld(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                if (ModuleManagementCommands.curWorldIdOfConsole == null)
                {
                    invalidUsage(context, "roles", "&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                }
                world = this.worldManager.getWorld(ModuleManagementCommands.curWorldIdOfConsole);
                context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
            }
            else
            {
                world = this.worldManager.getWorld((Long) sender.getAttribute(this.module, "curWorldId"));
                if (world == null)
                {
                    world = sender.getWorld();
                }
                else
                {
                    context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
                }
            }
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
            }
        }
        return world;
    }

    protected Role getRole(CommandContext context, RoleProvider provider, String name, World world)
    {
        Role role = provider.getRole(name);
        if (role == null)
        {
            if (world == null)
            {
                paramNotFound(context, "roles", "&cCould not find the global role &6%s&c.", name);
            }
            else
            {
                paramNotFound(context, "roles", "&cCould not find the role &6%s &cin &6%s&c.", name, world.getName());
            }
        }
        return role;
    }
}
