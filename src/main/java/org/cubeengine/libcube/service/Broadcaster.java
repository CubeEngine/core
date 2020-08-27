/*
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
package org.cubeengine.libcube.service;


import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collection;


public class Broadcaster
{
    @Inject private I18n i18n;

    private Audience getAll()
    {
        return Sponge.getServer().getBroadcastAudience();
    }

    private Collection<ServerPlayer> getOnlinePlayers()
    {
        return Sponge.getServer().getOnlinePlayers();
    }


    public void broadcastTranslatedWithPerm(Style format, String message, String perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        if (perm == null)
        {
            i18n.send(Audiences.server(), format, message, params);
        }
        else
        {
            // TODO Audiences.withPermission
            i18n.send(Audiences.system(), format, message, params);
            getOnlinePlayers().stream().filter(p -> p.hasPermission(perm)).forEach(p -> i18n.send(p, format, message, params));
        }
    }

    public void broadcastMessageWithPerm(Style format, String message, String perm, Object... params)
    {
        if (message.isEmpty())
        {
            return;
        }
        if (perm == null)
        {
            final Audience audience = Audiences.server();
            audience.sendMessage(i18n.composeMessage(audience, format, message, params));
        }
        else
        {
            final Audience audience = Audiences.system();
            audience.sendMessage(i18n.composeMessage(audience, format, message, params));

            getOnlinePlayers().stream().filter(p -> p.hasPermission(perm)).forEach(p ->
                    p.sendMessage(i18n.composeMessage(p, format, message, params)));
        }

    }

    public void broadcastTranslated(Style format, String message, Object... params)
    {
        this.broadcastTranslatedWithPerm(format, message, null, params);
    }

    public void broadcastMessage(Style format, String message, Object... params)
    {
        this.broadcastMessageWithPerm(format, message, null, params);
    }

    public void broadcastStatus(Style starColor, String message, CommandCause sender, Object... params)
    {
        final String causeName = sender.getSubject().getFriendlyIdentifier().orElse(sender.getSubject().getIdentifier());
        for (Player user : this.getOnlinePlayers())
        {
            user.sendMessage(i18n.composeMessage(user.getLocale(), starColor, "* {user} {input#message:color=WHITE}",
                    causeName, message, params));
        }
    }

    public void broadcastTranslatedStatus(Style starColor, String message, CommandCause sender, Object... params)
    {
        final String causeName = sender.getSubject().getFriendlyIdentifier().orElse(sender.getSubject().getIdentifier());
        for (Player user : this.getOnlinePlayers())
        {
            user.sendMessage(i18n.composeMessage(user.getLocale(), starColor, "* {user} {txt#message:color=WHITE}", causeName,
                                                 i18n.translate(user, message, params)));
        }
    }

    public void broadcastStatus(String message, CommandCause sender, Object... params)
    {
        this.broadcastStatus(Style.of(NamedTextColor.WHITE), message, sender, params);
    }

    public synchronized void kickAll(String message)
    {
        getOnlinePlayers().forEach(p -> p.kick(i18n.translate(p, message)));
    }
}
