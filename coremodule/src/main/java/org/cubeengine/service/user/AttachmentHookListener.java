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
package org.cubeengine.service.user;

import com.google.common.base.Optional;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class AttachmentHookListener
{
    private SpongeUserManager um;

    public AttachmentHookListener(SpongeUserManager um)
    {
        this.um = um;
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getTargetEntity().getUniqueId()).getAll())
        {
            attachment.onJoin(event.getMessage());
        }
    }

    @Listener(order = Order.POST)
    public void onQuit(ClientConnectionEvent.Disconnect event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getTargetEntity().getUniqueId()).getAll())
        {
            attachment.onQuit(event.getMessage());
        }
    }

    @Listener(order = Order.POST)
    public void onKick(KickPlayerEvent event)
    {
        for (UserAttachment attachment : um.getExactUser(event.getTargetEntity().getUniqueId()).getAll())
        {
            attachment.onKick(event.getMessage());
        }
    }

    @Listener(order = Order.POST)
    public void onChat(MessageSinkEvent event)
    {
        Optional<Player> source = event.getCause().first(Player.class);
        if (!source.isPresent())
        {
            return;
        }
        for (UserAttachment attachment : um.getExactUser(source.get().getUniqueId()).getAll())
        {
            attachment.onChat(event.getOriginalMessage());
        }
    }

    @Listener(order = Order.POST)
    public void onCommand(SendCommandEvent event)
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
