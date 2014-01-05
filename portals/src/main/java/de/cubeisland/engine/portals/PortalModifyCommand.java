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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
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
             usage = "here|<world>", min = 1, max = 1)
    public void destination(CommandContext context)
    {
        Portal portal = null;
        if (context.getSender() instanceof User)
        {
            portal = ((User)context.getSender()).attachOrGet(PortalsAttachment.class, getModule()).getPortal();
        }
        // TODO named portal param
        if (portal == null)
        {
            context.sendTranslated("You have to select a portal!");
            return;
        }
        if (context.getString(0).equalsIgnoreCase("here"))
        {
            if (context.getSender() instanceof User)
            {
                portal.config.destination = new Destination(((User)context.getSender()).getLocation());
                portal.config.save();
                context.sendTranslated("&aPortal destination set!");
                return;
            }
            context.sendTranslated("&eThe Portal Agency will bring you your portal for just &6$ 1337&e within &6%d weeks",
                                   new Random().nextInt(51)+1);
            return;
        }
        World world = this.getModule().getCore().getWorldManager().getWorld(context.getString(0));
        context.sendMessage("TODO");
        // TODO
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
