package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import org.bukkit.World;

public abstract class RoleCommandHelper extends ContainerCommand
{
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
                invalidUsage(context, "roles", "&ePlease provide a world.");//TODO msg can set world to use with cmd
            }
            //TODO get default world if set
            world = sender.getWorld();
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

    protected RoleProvider getProvider(World world)
    {
        return this.manager.getProvider(this.worldManager.getWorldId(world));
    }
}
