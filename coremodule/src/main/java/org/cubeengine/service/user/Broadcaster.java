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

import java.util.Collection;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.asm.marker.Version;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextFormat;

import static org.cubeengine.service.i18n.formatter.MessageType.NONE;
import static org.spongepowered.api.text.format.TextColors.WHITE;

@ServiceProvider(Broadcaster.class)
@Version(1)
public class Broadcaster
{
    @Inject private Game game;
    @Inject private I18n i18n;

    private Collection<MessageReceiver> getAll()
    {
        return MessageChannel.TO_ALL.getMembers();
    }

    private Collection<Player> getOnlinePlayers()
    {
        return game.getServer().getOnlinePlayers();
    }


    public void broadcastTranslatedWithPerm(TextFormat format, String message, String perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        MessageChannel.permission(perm).getMembers().stream()
                .forEach(s -> i18n.sendTranslated(s, format, message, params));
    }

    public void broadcastMessageWithPerm(TextFormat format, String message, String perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        MessageChannel.permission(perm).getMembers().stream()
                .forEach(s -> s.sendMessage(i18n.composeMessage(i18n.getLocale(s), format, message, params)));
    }

    public void broadcastTranslated(TextFormat format, String message, Object... params)
    {
        this.broadcastTranslatedWithPerm(format, message, null, params);
    }

    public void broadcastMessage(TextFormat format, String message, Object... params)
    {
        this.broadcastMessageWithPerm(format, message, null, params);
    }

    public void broadcastStatus(TextFormat starColor, String message, CommandSource sender, Object... params)
    {
        for (Player user : this.getOnlinePlayers())
        {
            user.sendMessage(i18n.composeMessage(user.getLocale(), starColor, "* {user} {input#message:color=WHITE}",
                                                 sender.getName(), message));
        }
    }

    public void broadcastTranslatedStatus(TextFormat starColor, String message, CommandSource sender, Object... params)
    {
        for (Player user : this.getOnlinePlayers())
        {
            user.sendMessage(i18n.composeMessage(user.getLocale(), starColor, "* {user} {input#message:color=WHITE}", sender.getName(),
                                                 i18n.getTranslation(user, NONE, message)));
        }
    }

    public void broadcastStatus(String message, CommandSource sender, Object... params)
    {
        this.broadcastStatus(NONE.color(WHITE), message, sender, params);
    }

    public synchronized void kickAll(String message)
    {
        getOnlinePlayers().forEach(p -> p.kick(i18n.getTranslation(p.getLocale(), NONE, message)));
    }
}
