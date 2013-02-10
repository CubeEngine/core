package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;

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

    protected World getWorld(ParameterizedContext context)
    {
        World world;
        if (!context.hasParam("in"))
        {
            CommandSender sender = context.getSender();
            if (sender instanceof User)
            {
                User user = (User)sender;
                world = this.worldManager.getWorld((Long)user.getAttribute(this.module, "curWorldId"));
                if (world == null)
                {
                    world = user.getWorld();
                }
                else
                {
                    context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
                }
            }
            else
            {
                if (ModuleManagementCommands.curWorldIdOfConsole == null)
                {
                    invalidUsage(context, "roles", "&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                }
                world = this.worldManager.getWorld(ModuleManagementCommands.curWorldIdOfConsole);
                context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
            }
        }
        else
        {
            world = context.getParam("in");
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
