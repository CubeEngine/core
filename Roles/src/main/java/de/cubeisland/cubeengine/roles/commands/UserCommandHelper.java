package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.exception.MissingParameterException;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.World;

public class UserCommandHelper extends ContainerCommand
{
    protected RoleManager manager;
    protected WorldManager worldManager;
    protected Roles module;

    public UserCommandHelper(Roles module)
    {
        super(module, "user", "Manage users.");
        this.manager = module.getManager();
        this.worldManager = module.getCore().getWorldManager();
        this.module = module;
    }

    protected User getUser(CommandContext context, int pos)
    {
        User user = null;
        if (context.hasArg(pos))
        {
            user = context.getUser(pos);
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            if (user == null)
            {
                context.sendMessage("roles", "&cYou have to specify a player.");
                throw new MissingParameterException("user"); //TODO this is bullshit
            }
        }
        if (user == null)
        {
            context.sendMessage("roles", "&cUser %s not found!", context.getString(pos));
            throw new MissingParameterException("user"); //TODO this is bullshit
        }
        return user;
    }

    protected UserSpecificRole getUserRole(User user, World world)
    {
        long worldId = this.getWorldId(world);
        if (user == null)
        {
            return null;
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            this.manager.preCalculateRoles(user, true);
            roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        }
        return roleContainer.get(worldId);
    }

    protected long getWorldId(World world)
    {
        return this.getModule().getCore().getWorldManager().getWorldId(world);
    }

    /**
     * Returns the world defined with named param "in" or the users world
     *
     * @param context
     * @return
     */
    protected World getWorld(ParameterizedContext context)
    {
        World world;
        if (context.hasParam("in"))
        {
            world = context.getParam("in");
            if (world == null)
            {
                context.sendMessage("roles", "&cWorld %s not found!", context.getString("in"));
                throw new MissingParameterException("world"); //TODO this is bullshit
            }
        }
        else
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
                world = this.worldManager.getWorld(ModuleManagementCommands.curWorldIdOfConsole);
                if (world == null)
                {
                    context.sendMessage("roles", "&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                    throw new MissingParameterException("world"); //TODO this is bullshit
                }
                else
                {
                    context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
                }
            }
        }
        return world;
    }
}
