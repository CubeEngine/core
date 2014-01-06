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
package de.cubeisland.engine.roles.commands;

import org.bukkit.World;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.RolesManager;

public class UserCommandHelper extends ContainerCommand
{
    protected RolesManager manager;
    protected WorldManager worldManager;
    protected Roles module;
    protected final String LISTELEM_VALUE = ChatFormat.parseFormats("- &e%s&f: &6%s");
    protected final String LISTELEM = ChatFormat.parseFormats("- &e%s");

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
                return null;
            }
        }
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(pos));
            return null;
        }
        return user;
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
            }
            return world;
        }
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            world = user.attachOrGet(RolesAttachment.class, this.module).getWorkingWorld();
            if (world == null)
            {
                world = user.getWorld();
            }
            else
            {
                context.sendTranslated("&eYou are using &6%s&e as current world.", world.getName());
            }
            return world;
        }
        if (ManagementCommands.curWorldOfConsole == null)
        {
            context.sendTranslated("&ePlease provide a world.");
            context.sendTranslated("&aYou can define a world with &6/roles admin defaultworld <world>");
            return null;
        }
        world = ManagementCommands.curWorldOfConsole;
        context.sendTranslated("&eYou are using &6%s&e as current world.", world.getName());
        return world;
    }
}
