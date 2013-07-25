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
package de.cubeisland.engine.basics.command.moderation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsPerm;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

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
    private BanManager banManager;
    private UserManager um;

    public KickBanCommands(Basics module)
    {
        this.module = module;
        this.banManager = this.module.getCore().getBanManager();
        this.um = this.module.getCore().getUserManager();
    }

    @Command(desc = "Kicks a player from the server",
             usage = "<player or *> [reason]",
             flags = @Flag(longName = "all", name = "a"),min = 1, max = 2)
    public void kick(ParameterizedContext context)
    {
        String reason;
        if (context.hasArg(1))
        {
            reason = context.getStrings(1);
        }
        else
        {
            if (!BasicsPerm.COMMAND_KICK_NOREASON.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou need to specify a kick-reason!");
                return;
            }
            reason = this.module.getConfiguration().defaultKickMessage;
        }
        reason = ChatFormat.parseFormats(reason);
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
                    player.kickPlayer(reason);
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
        user.kickPlayer(reason);
        context.getCore().getUserManager().broadcastMessage("&2%s &4was kicked from the server!", BasicsPerm.KICK_RECEIVEMESSAGE, user.getName());
    }

    // TODO refactor me place @Faithcaio!!!
    @Command(names = {
    "ban", "kickban"
    }, desc = "Bans a player permanently on your server.", min = 1, max = NO_MAX,
             usage = "<player> [reason] [-ipban]",
             flags = {@Flag(longName = "ipban", name = "ip"),
             @Flag(longName = "force", name = "f")})
    public void ban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context))
        {
            return;
        }
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!player.hasPlayedBefore() && context.hasFlag("f"))
        {
            context.sendTranslated("&2%s&6 has never played on this server before! Use the -force flag to ban him anyways.", player.getName());
            return;
        }
        User user = context.getCore().getUserManager().getExactUser(player.getName());
        if (context.hasFlag("ip"))
        {
            String reason =  this.getReasonFrom(context, 1, user, BasicsPerm.COMMAND_BAN_NOREASON, true);
            if (reason == null) return;
            if (user.getAddress() != null)
            {
                InetAddress ipAdress = user.getAddress().getAddress();
                if (this.banManager.isIpBanned(ipAdress))
                {
                    context.sendTranslated("&2%s&c is already ip-banned!", player.getName());
                    return;
                }
                this.banManager.addBan(new IpBan(ipAdress,context.getSender().getName(), reason));
                for (User ipPlayer : this.module.getCore().getUserManager().getOnlineUsers())
                {
                    if (ipPlayer.getAddress() != null
                        && ipPlayer.getAddress().getAddress().equals(ipAdress))
                    {
                        ipPlayer.kickPlayer(
                            reason == null ?  reason = user.translate("&cYou got ip-banned from this server!") : reason);
                    }
                }
                context.sendTranslated("&cYou banned the IP: &e%s&c!", ipAdress.getHostAddress());
            }
            else
            {
                context.sendTranslated("&eYou cannot IP-ban this player because he was offline for too long!");
            }
        }
        else
        {
            String reason = this.getReasonFrom(context, 1, user, BasicsPerm.COMMAND_BAN_NOREASON, false);
            if (reason == null) return;
            if (this.banManager.isUserBanned(user))
            {
                context.sendTranslated("&2%s&c is already banned!", player.getName());
                return;
            }
            this.banManager.addBan(new UserBan(user.getName(),context.getSender().getName(),reason == null ?
                    user.translate("&cYou got ip-banned from this server!") : reason));
            user.kickPlayer(reason);
        }
        context.sendTranslated("&cYou banned &2%s&c!", player.getName());
    }

    private String getReasonFrom(CommandContext context, int at, User forUser, Permission permNeeded, Boolean ipban)
    {
        String reason;
        if (context.hasArg(1))
        {
            reason = ChatFormat.parseFormats(context.getStrings(at));
        }
        else
        {
            if (!permNeeded.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou need to specify a ban-reason!");
                return null;
            }
            if (forUser == null)
                return null;
            if (ipban == null)
            {
                reason = forUser.translate("&cYou got kicked from the server!");
            }
            else if (ipban)
            {
                reason = forUser.translate("&cYou got ip-banned from this server!");
            }
            else
            {
                reason = forUser.translate("&cYou got banned from this server!");
            }
        }
        return reason;
    }

    @Command(names = {
    "unban", "pardon"
    }, desc = "Unbans a previously banned player.", min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        String userName = context.getString(0);
        if (!this.banManager.isUserBanned(userName))
        {
            context.sendTranslated("&2%s &cis not banned, maybe you misspelled his name?", userName);
            return;
        }
        this.banManager.removeUserBan(userName);
        context.sendTranslated("&aYou unbanned &2%s&a!", userName);
    }

    @Command(names = {
    "ipban", "banip"
    }, desc = "Bans the IP from this server.", min = 1, max = 2, usage = "<IP address> [reason]")
    public void ipban(CommandContext context)
    {
        String ipaddress = context.getString(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipaddress);
            if (this.banManager.isIpBanned(address))
            {
                context.sendTranslated("&eThe IP &6%s&e is already banned!", address.getHostAddress());
                return;
            }
            String reason = this.getReasonFrom(context,1,null,BasicsPerm.COMMAND_IPBAN_NOREASON, true);
            this.banManager.addBan(new IpBan(address,context.getSender().getName(),reason == null ?
                         ChatFormat.parseFormats("&cYou were ip-banned from this server!") : reason));
            context.sendTranslated("&cYou banned the IP &6%s &cfrom your server!", address.getHostAddress());
            for (User user : context.getCore().getUserManager().getOnlineUsers())
            {
                if (user.getAddress() != null && user.getAddress().getAddress().getHostAddress().equals(ipaddress))
                {
                    user.kickPlayer(reason == null ? user.translate("&cYou were ip-banned from this server!") : reason);
                }
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated("&6%s&c is not a valid IP-address!", ipaddress);
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
            InetAddress address = InetAddress.getByName(ipadress);
            this.banManager.removeIpBan(address);
            context.sendTranslated("&aYou unbanned the IP &6%s&a!", address.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated("&6%s&c is not a valid IP-address!", ipadress);
        }
    }

    // TODO refactor me place @Faithcaio!!!
    @Command(names = {"tempban","tban"},
             desc = "Bans a player for a given time.",
             min = 2, max = 3,
             usage = "<player> <time> [reason]",
             flags = @Flag(longName = "force", name = "f"))
    public void tempban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context))
        {
            return;
        }
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!player.hasPlayedBefore() && context.hasFlag("f"))
        {
            context.sendTranslated("&2%s&6 has never played on this server before! Use the -force flag to ban him anyways.", player.getName());
            return;
        }
        User user = context.getCore().getUserManager().getExactUser(player.getName());
        String reason = this.getReasonFrom(context, 2, user, BasicsPerm.COMMAND_TEMPBAN_NOREASON, false);
        if (reason == null) return;
        if (this.banManager.isUserBanned(player.getName()))
        {
            context.sendTranslated("&2%s&c is already banned!", player.getName());
            return;
        }
        try
        {
            long millis = StringUtils.convertTimeToMillis(context.getString(1));
            Date toDate = new Date(System.currentTimeMillis() + millis);
            this.banManager.addBan(new UserBan(user.getName(),context.getSender().getName(), reason, toDate));
            user.kickPlayer(reason);
            context.sendTranslated("&aYou banned &2%s&a temporarily!",user.getName());
        }
        catch (ConversionException ex)
        {
            context.sendTranslated("&cInvalid time value! &eExamples: 1d 12h 5m");
        }
    }

    private boolean cannotBanUser(CommandContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().disallowBanIfOfflineMode)
            {
                context.sendTranslated("&cBanning players by name is not allowed in offline-mode!\n"
                                     + "&eYou can change this in your Basics-Configuration.");
                return true;
            }
            context.sendTranslated("&eThe server is running in &4OFFLINE-mode&e.\n"
                                 + "Players could change their username with a cracked client!\n"
                                 + "&aYou can IP-ban to prevent banning a real player in that case.");
        }
        return false;
    }

    @Command(desc = "Reloads the ban lists")
    public void reloadbans(CommandContext context)
    {
        this.banManager.reloadBans();
        context.sendTranslated("&aReloaded the ban lists successfully!");
    }
}
