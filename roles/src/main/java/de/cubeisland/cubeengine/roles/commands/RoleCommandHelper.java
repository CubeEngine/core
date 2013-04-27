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
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.RolesManager;

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
                Long worldId = user.attachOrGet(RolesAttachment.class,this.module).getWorkingWorldId();
                if (worldId == null)
                {
                    world = user.getWorld();
                }
                else
                {
                    world = this.worldManager.getWorld(worldId);
                    context.sendTranslated("&eYou are using &6%s&e as current world.", world.getName());
                }
            }
            else
            {
                if (ManagementCommands.curWorldIdOfConsole == null)
                {
                    context.sendTranslated("&ePlease provide a world.");
                    context.sendTranslated("&aYou can define a world with &6/roles admin defaultworld <world>");
                    return null;
                }
                world = this.worldManager.getWorld(ManagementCommands.curWorldIdOfConsole);
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
