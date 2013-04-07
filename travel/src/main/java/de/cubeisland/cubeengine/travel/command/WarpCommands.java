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
package de.cubeisland.cubeengine.travel.command;

import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.Warp;

public class WarpCommands
{
    private final TelePointManager tpManager;

    public WarpCommands(Travel module)
    {
        this.tpManager = module.getTelepointManager();
    }

    @Command(desc = "Teleport to a warp", min = 1, max = 1)
    public void warp(CommandContext context)
    {
        if (context.getSender() instanceof  User)
        {
            User sender = (User) context.getSender();
            Warp warp = tpManager.getWarp(sender, context.getString(0).toLowerCase());
            if (warp == null)
            {
                context.sendTranslated("&4You don't have access to any warp with that name");
                return;
            }

            sender.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            context.sendTranslated("&6You have been teleported to the warp &9%s", context.getString(0));
        }
        else
        {
            context.sendTranslated("&4This command can only be used by users!");
        }

    }
}
