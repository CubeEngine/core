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
package de.cubeisland.engine.service.user;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerKickEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.event.message.CommandEvent;

public class AttachmentHookListener
{
    private SpongeUserManager um;

    public AttachmentHookListener(SpongeUserManager um)
    {
        this.um = um;
    }

    @Subscribe(order = Order.POST)
    public void onJoin(PlayerJoinEvent event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getUser().getUniqueId()).getAll())
        {
            attachment.onJoin(event.getMessage());
        }
    }

    @Subscribe(order = Order.POST)
    public void onQuit(PlayerQuitEvent event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getUser().getUniqueId()).getAll())
        {
            attachment.onQuit(event.getMessage());
        }
    }

    @Subscribe(order = Order.POST)
    public void onKick(PlayerKickEvent event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getUser().getUniqueId()).getAll())
        {
            attachment.onKick(event.getMessage());
        }
    }

    @Subscribe(order = Order.POST)
    public void onChat(PlayerChatEvent event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getUser().getUniqueId()).getAll())
        {
            attachment.onChat(event.getUnformattedMessage());
        }
    }

    @Subscribe(order = Order.POST)
    public void onCommand(CommandEvent event)
    {
        if (event.getSource() instanceof Player)
        {
            for (UserAttachment attachment : um.getExactUser(((Player)event.getSource()).getUniqueId()).getAll())
            {
                attachment.onCommand(event.getCommand() + " " + event.getArguments());
            }
        }
    }
}
