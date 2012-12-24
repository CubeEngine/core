package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.World;

public class UserCommandHelper extends ContainerCommand
{
    protected RoleManager manager;

    public UserCommandHelper(Roles module)
    {
        super(module, "user", "Manage users.");//TODO alias manuser
        this.manager = module.getManager();
    }

    protected User getUser(CommandContext context, int pos)
    {
        User user;
        if (context.hasIndexed(pos))
        {
            user = context.getUser(pos);
        }
        else
        {
            user = context.getSenderAsUser("roles", "&cYou have to specify a player.");
        }
        if (user == null)
        {
            paramNotFound(context, "roles", "&cUser %s not found!", context.getString(pos));
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
            if (user.isOnline())
            {
                throw new IllegalStateException("User has no rolecontainer!");
            }
            else
            {
                this.manager.preCalculateRoles(user.getName(), true);
                roleContainer = user.getAttribute(this.getModule(), "roleContainer");
            }
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
     * @param user
     * @return
     */
    protected World getWorld(CommandContext context, User user)
    {
        //TODO consider setting for default world always show which world is set
        World world = context.hasNamed("in") ? context.getNamed("in", World.class) : user.getWorld();
        if (world == null)
        {
            paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
        }
        return world;
    }
}
