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

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;

public class ManagementCommands extends ContainerCommand
{
    public ManagementCommands(Roles module)
    {
        super(module, "admin", "Manages the module.");
        this.registerAlias(new String[]{"manadmin"},new String[]{});
    }

    @Alias(names = "manload")
    @Command(desc = "Reloads all roles from config")
    public void reload(CommandContext context)
    {
        Roles module = (Roles)this.getModule();
        module.getConfiguration().reload();
        module.getRolesManager().initRoleProviders();
        module.getRolesManager().recalculateAllRoles();
        context.sendTranslated("&f[&6Roles&f]&a reload complete!");
    }

    @Alias(names = "mansave")
    @Command(desc = "Overrides all configs with current settings")
    public void save(CommandContext context)
    {
        // database is up to date so only saving configs
        Roles module = (Roles)this.getModule();
        module.getConfiguration().save();
        module.getRolesManager().saveAll();
        context.sendTranslated("&f[&6Roles&f]&a all configurations saved!");
    }

    public static Long curWorldIdOfConsole = null;

    @Command(desc = "Sets or resets the current default world",
             usage = "[world]", max = 1)
    public void defaultworld(CommandContext context)
    {
        Long worldId = null;
        if (context.hasArg(0))
        {
            worldId = this.getModule().getCore().getWorldManager().getWorldId(context.getString(0));
            if (worldId == null)
            {
                context.sendTranslated("&cInvalid world! No world &6%s &cfound", context.getString(0));
                return;
            }
            context.sendTranslated("&aAll your roles commands will now have &6%s&a as default world!", context.getString(0));
        }
        else
        {
            context.sendTranslated("&eCurrent world for roles resetted!");
        }
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            if (worldId == null)
            {
                ((User)sender).get(RolesAttachment.class).setWorkingWorldId(null);
                return;
            }
            ((User)sender).get(RolesAttachment.class).setWorkingWorldId(worldId);
            return;
        }
        if (context.hasArg(0))
        {
            curWorldIdOfConsole = worldId;
            return;
        }
        curWorldIdOfConsole = null;
    }
}
