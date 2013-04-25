/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.roles.commands;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.exception.MissingParameterException;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.RoleManager;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.RolesManager;

import gnu.trove.map.hash.TLongObjectHashMap;

public class UserCommandHelper extends ContainerCommand
{
    protected RolesManager manager;
    protected WorldManager worldManager;
    protected Roles module;

    public UserCommandHelper(Roles module)
    {
        super(module, "user", "Manage users.");
        this.manager = module.getRolesManager();
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
                context.sendTranslated("&cYou have to specify a player.");
                throw new MissingParameterException("user"); //TODO this is bullshit
            }
        }
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(pos));
            throw new MissingParameterException("user"); //TODO this is bullshit
        }
        return user;
    }

    protected UserSpecificRole getUserRole(User user, World world)
    {
        long worldId = this.getWorldId(world);
        worldId = this.manager.getUserMirror(worldId);
        if (user == null)
        {
            return null;
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = this.manager.getRoleContainer(user);
        if (roleContainer == null)
        {
            this.manager.preCalculateRoles(user, true);
            roleContainer = this.manager.getRoleContainer(user);
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
                context.sendTranslated("&cWorld %s not found!", context.getString("in"));
                throw new MissingParameterException("world"); //TODO this is bullshit
            }
        }
        else
        {
            CommandSender sender = context.getSender();
            if (sender instanceof User)
            {
                User user = (User)sender;
                Long worldID = user.attachOrGet(RolesAttachment.class, this.module).getCurrentWorldId();
                if (worldID == null)
                {
                    world = user.getWorld();
                }
                else
                {
                    world = this.worldManager.getWorld(worldID);
                    context.sendTranslated("&eYou are using &6%s &eas current world.", world.getName());
                }
            }
            else
            {
                if (ManagementCommands.curWorldIdOfConsole == null)
                {
                    context.sendTranslated("&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                    throw new MissingParameterException("world"); //TODO this is bullshit
                }
                else
                {
                    world = this.worldManager.getWorld(ManagementCommands.curWorldIdOfConsole);
                    context.sendTranslated("&eYou are using &6%s &eas current world.", world.getName());
                }
            }
        }
        return world;
    }
}
