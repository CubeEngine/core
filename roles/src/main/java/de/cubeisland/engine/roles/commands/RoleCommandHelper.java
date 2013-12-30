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
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RoleProvider;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.RolesManager;

public abstract class RoleCommandHelper extends ContainerCommand
{
    protected static final String GLOBAL_PREFIX = "g:";
    protected RolesManager manager;
    protected Roles module;
    protected WorldManager worldManager;

    protected final String LISTELEM = ChatFormat.parseFormats("- &e%s");
    protected final String LISTELEM_VALUE = ChatFormat.parseFormats("- &e%s&f: &6%s");

    public RoleCommandHelper(Roles module)
    {
        super(module, "role", "Manage roles.");
        this.manager = module.getRolesManager();
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
                world = user.attachOrGet(RolesAttachment.class,this.module).getWorkingWorld();
                if (world == null)
                {
                    world = user.getWorld();
                }
                else
                {
                    context.sendTranslated("&eYou are using &6%s&e as current world.", world.getName());
                }
            }
            else
            {
                if (ManagementCommands.curWorldOfConsole == null)
                {
                    context.sendTranslated("&cYou have to provide a world with &6[in <world]&c!");
                    context.sendTranslated("&eOr you can define a default-world with &6/roles admin defaultworld <world>");
                    return null;
                }
                world = ManagementCommands.curWorldOfConsole;
                context.sendTranslated("&eYou are using &6%s&e as current world.", world.getName());
            }
        }
        else
        {
            world = context.getParam("in");
            if (world == null)
            {
                context.sendTranslated("&cWorld %s not found!", context.getString("in"));
                return null;
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
                context.sendTranslated("&cCould not find the global role &6%s&c.", name);
                return null;
            }
            else
            {
                context.sendTranslated("&cCould not find the role &6%s&c in &6%s&c.", name, world.getName());
                return null;
            }
        }
        return role;
    }
}
