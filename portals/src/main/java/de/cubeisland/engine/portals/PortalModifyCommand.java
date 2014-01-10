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
package de.cubeisland.engine.portals;

import java.util.Random;

import org.bukkit.World;

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.portals.config.Destination;

public class PortalModifyCommand extends ContainerCommand
{
    private PortalManager manager;

    public PortalModifyCommand(Portals module, PortalManager manager)
    {
        super(module, "modify", "modifies a portal");
        this.registerAlias(new String[]{"mvpm"}, new String[0]);
        this.manager = manager;
    }

    public void owner()
    {

    }

    @Alias(names = "mvpd")
    @Command(names = {"destination","dest"},
        desc = "changes the destination of the selected portal",
             usage = "here|<world>|<p:<portal>> [p <portal>]", min = 1, max = 1,
    params = @Param(names = {"p","portal"}))
    public void destination(ParameterizedContext context)
    {
        Portal portal = null;
        if (context.hasParam("p"))
        {
            portal = manager.getPortal(context.getString("p"));
            if (portal == null)
            {
                context.sendTranslated("&cPortal &6%s&c not found!", context.getString("p"));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        if (portal == null)
        {
            context.sendTranslated("&cYou need to define a portal!");
            context.sendMessage(context.getCommand().getUsage(context));
            return;
        }
        if (context.getString(0).equalsIgnoreCase("here"))
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated("&eThe Portal Agency will bring you your portal for just &6$ 1337&e within &6%d weeks",
                                       new Random().nextInt(51)+1);
                return;
            }
            portal.config.destination = new Destination(((User)context.getSender()).getLocation());
        }
        else if (context.getString(0).startsWith("p:"))
        {
            Portal destPortal = manager.getPortal(context.getString(0).substring(2));
            if (destPortal == null)
            {
                context.sendTranslated("&cPortal &6%s&c not found!", context.getString(0).substring(2));
                return;
            }
            portal.config.destination = new Destination(destPortal);
        }
        else
        {
            World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
            if (world == null)
            {
                context.sendTranslated("&cWorld &6%s&c not found!", context.getString(0));
                return;
            }
            portal.config.destination = new Destination(world);
        }
        portal.config.save();
        context.sendTranslated("&aPortal destination set!");
    }

    public void location()
    {

    }

    public void safe()
    {

    }

    public void entity()
    {

    }
}
