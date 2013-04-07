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
package de.cubeisland.cubeengine.basics.command.moderation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;

/**
 * Contains commands to manage kicks/bans.
 * /kick
 * /ban
 * /unban
 * /ipban
 * /ipunban
 */
public class KickBanCommands
{
    private Basics module;

    public KickBanCommands(Basics module)
    {
        this.module = module;
    }

    @Command(desc = "Kicks a player from the server", usage = "<player or *> [message]", flags = @Flag(longName = "all", name = "a"),min = 1, max = 2)
    public void kick(ParameterizedContext context)
    {
        String message;
        if (context.hasArg(1))
        {
            message = context.getStrings(1);
        }
        else
        {
            if (!BasicsPerm.COMMAND_KICK_NOREASON.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou need to specify a kick-reason!");
                return;
            }
            message = this.module.getConfiguration().defaultKickMessage;
        }
        message = ChatFormat.parseFormats(message);
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to kick everyone!");
                return;
            }
            String sendername = context.getSender().getName();
            for (Player player : context.getSender().getServer().getOnlinePlayers())
            {
                if (!sendername.equalsIgnoreCase(player.getName()))
                {
                    player.kickPlayer(message);
                }
            }
            return;
        }
        if (!context.hasArg(0))
        {
            context.sendTranslated("&cYou need to specify a player!");
            return;
        }
        User user = context.getUser(0);
        if (user == null && !context.hasFlag("a"))
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        user.kickPlayer(message);
        context.getCore().getUserManager().broadcastMessage("&2%s &4was kicked from the server!", BasicsPerm.KICK_RECEIVEMESSAGE, user.getName());
    }

    @Command(names = {
    "ban", "kickban"
    }, desc = "Bans a player permanently on your server.", min = 1, max = NO_MAX, usage = "<player> [message] [-ipban]", flags = @Flag(longName = "ipban", name = "ip"))
    public void ban(ParameterizedContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().disallowBanIfOfflineMode)
            {
                context.sendTranslated("&cBanning players by name is not allowed in offline-mode!"
                                           + "\n&eYou can change this in your Basics-Configuration.");
                return;
            }
            context.sendTranslated("&eThe server is running in &4OFFLINE-mode&e. "
                                       + "\nPlayers could change their username with a cracked client!\n"
                                       + "&aYou can IP-ban to prevent banning a real player in that case.");
        }
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (player.isBanned())
        {
            context.sendTranslated("&2%s &cis already banned!", player.getName());
            return;
        }
        if (player.hasPlayedBefore() == false)
        {
            context.sendTranslated("&2%s &6has never played on this server before!", player.getName());
            return;
        }
        else if (context.hasFlag("ip"))
        {
            User user = this.module.getCore().getUserManager().getExactUser(player);
            if (user.getAddress() != null)
            {
                String ipadress = user.getAddress().getAddress().getHostAddress();
                Bukkit.banIP(ipadress);
                for (User ipPlayer : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (!ipPlayer.getName().equals(player.getName())
                        && ipPlayer.getAddress() != null
                        && ipPlayer.getAddress().getAddress().getHostAddress().equals(ipadress))
                    {
                        ipPlayer.kickPlayer(ipPlayer.translate("&cYou were ip-banned from this server!"));
                    }
                }
                context.sendTranslated("&cYou banned the IP: &e%s&c!", ipadress);
            }
            else
            {
                context.sendTranslated("&eYou cannot IP-ban this player because he was offline for too long!");
            }
        }
        if (player.isOnline())
        {
            User user = context.getCore().getUserManager().getExactUser(player);
            String message;
            if (context.hasArg(1))
            {
                message = ChatFormat.parseFormats(context.getStrings(1));
            }
            else
            {
                if (!BasicsPerm.COMMAND_BAN_NOREASON.isAuthorized(context.getSender()))
                {
                    context.sendTranslated("&cYou need to specify a ban-reason!");
                    return;
                }
                message = user.translate("&cYou got banned from this server!");
            }
            user.kickPlayer(message);
        }
        player.setBanned(true);
        context.sendTranslated("&cYou banned &2%s&c!", player.getName());
    }

    @Command(names = {
    "unban", "pardon"
    }, desc = "Unbans a previously banned player.", min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        OfflinePlayer offlinePlayer = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!offlinePlayer.isBanned())
        {
            context.sendTranslated("&2%s &cis not banned!", offlinePlayer.getName());
            return;
        }
        offlinePlayer.setBanned(false);
        context.sendTranslated("&aYou unbanned &2%s&a!", offlinePlayer.getName());
    }

    @Command(names = {
    "ipban", "banip"
    }, desc = "Bans the IP from this server.", min = 1, max = 1, usage = "<IP address>")
    public void ipban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress adress = InetAddress.getByName(ipadress);
            Bukkit.banIP(adress.getHostAddress());
            context.sendTranslated("&cYou banned the IP &6%s &cfrom your server!", adress.getHostAddress());
            for (User user : context.getCore().getUserManager().getOnlineUsers())
            {
                if (user.getAddress() != null && user.getAddress().getAddress().getHostAddress().equals(ipadress))
                {
                    user.kickPlayer(user.translate("&cYou were banned from this server!"));
                }
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated("&6%s &cis not a valid IP-address!", ipadress);
        }
    }

    @Command(names = {
    "ipunban", "unbanip", "pardonip"
    }, desc = "Bans the IP from this server.", min = 1, max = 1, usage = "<IP address>")
    public void ipunban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress adress = InetAddress.getByName(ipadress);
            Bukkit.unbanIP(adress.getHostAddress());
            context.sendTranslated("&aYou unbanned the IP &6%s&a!", adress.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated("&6%s &cis not a valid IP-address!", ipadress);
        }
    }
}
