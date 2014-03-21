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
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import de.cubeisland.engine.basics.Basics;
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
import de.cubeisland.engine.core.util.TimeConversionException;
import de.cubeisland.engine.core.util.formatter.MessageType;

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
    private final Basics module;
    private final BanManager banManager;
    private final UserManager um;

    private static final String kickMessage = "You got kicked from the server!\n";
    private static final String banMessage = "You got banned from this server!\n";

    public KickBanCommands(Basics module)
    {
        this.module = module;
        this.banManager = this.module.getCore().getBanManager();
        this.um = this.module.getCore().getUserManager();
    }

    @Command(desc = "Kicks a player from the server",
             usage = "<*|<player>> [reason]", min = 1, max = NO_MAX)
    public void kick(ParameterizedContext context)
    {
        String reason;
        reason = this.getReasonFrom(context, 1, module.perms().COMMAND_KICK_NOREASON);
        if (reason == null) return;
        if (context.getString(0).equals("*"))
        {
            if (!module.perms().COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to kick everyone!");
                return;
            }
            String sendername = context.getSender().getName();
            for (User toKick : this.um.getOnlineUsers())
            {
                if (!sendername.equalsIgnoreCase(toKick.getName()))
                {
                    toKick.kickPlayer(toKick.translate(MessageType.NEGATIVE, kickMessage) + "\n" + ChatFormat.RESET + reason);
                }
            }
            return;
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        user.kickPlayer(user.translate(MessageType.NEGATIVE, kickMessage) + "\n" + ChatFormat.RESET + reason);
        this.um.broadcastMessageWithPerm(MessageType.NEGATIVE, "{user} was kicked from the server by {user}!\n{}", module.perms().KICK_RECEIVEMESSAGE, user, context.getSender(), reason);
    }

    @Command(names = {"ban", "kickban"},
             desc = "Bans a player permanently on your server.",
             min = 1, max = NO_MAX,
             usage = "<player> [reason] [-ipban]",
             flags = {@Flag(longName = "ipban", name = "ip"),
             @Flag(longName = "force", name = "f")})
    public void ban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getName());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(MessageType.NEGATIVE,"{user} has never played on this server before! Use the -force flag to ban him anyways.", player);
            return;
        }
        String reason = this.getReasonFrom(context, 1, module.perms().COMMAND_BAN_NOREASON);
        if (reason == null) return;
        if (context.hasFlag("ip"))
        {
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "You cannot ip-ban a player that has never played on the server before!");
                return;
            }
            if (user.getAddress() != null)
            {
                InetAddress ipAdress = user.getAddress().getAddress();
                if (this.banManager.isIpBanned(ipAdress))
                {
                    context.sendTranslated(MessageType.NEGATIVE, "{user} is already ip-banned!", player);
                    return;
                }
                this.banManager.addBan(new IpBan(ipAdress,context.getSender().getName(), reason));
                Set<String> bannedUsers = new HashSet<>();
                for (User ipPlayer : um.getOnlineUsers())
                {
                    if (ipPlayer.getAddress() != null && ipPlayer.getAddress().getAddress().equals(ipAdress))
                    {
                        ipPlayer.kickPlayer(ipPlayer.translate(MessageType.NEGATIVE, banMessage) + "\n" + ChatFormat.RESET + reason);
                        bannedUsers.add(ipPlayer.getName());
                    }
                }
                context.sendTranslated(MessageType.NEGATIVE, "You banned the IP: {input#ip}!", ipAdress.getHostAddress());
                um.broadcastMessageWithPerm(MessageType.NEGATIVE, "{user} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, user, context.getSender());
                um.broadcastMessageWithPerm(MessageType.NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
                um.broadcastMessageWithPerm(MessageType.NEGATIVE, "And with it kicked: {user#list}!", module.perms().BAN_RECEIVEMESSAGE,
                                            StringUtils.implode(ChatFormat.RED + "," + ChatFormat.DARK_GREEN, bannedUsers));
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "You cannot IP-ban this player because he was offline for too long!");
            }
            return;
        }
        else
        {
            if (this.banManager.isUserBanned(player.getName()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "{user} is already banned!", player);
                return;
            }
            this.banManager.addBan(new UserBan(player.getName(),context.getSender().getName(), reason));
            if (user != null)
            {
                user.kickPlayer(user.translate(MessageType.NEGATIVE, banMessage) + "\n" + ChatFormat.RESET + reason);
            }
        }
        context.sendTranslated(MessageType.NEGATIVE, "You banned {user}!", player);
        um.broadcastMessageWithPerm(MessageType.NEGATIVE, "{user} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, player, context.getSender());
        um.broadcastMessageWithPerm(MessageType.NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
    }

    private String getReasonFrom(CommandContext context, int at, Permission permNeeded)
    {
        String reason = "";
        if (context.hasArg(at))
        {
            reason = ChatFormat.parseFormats(context.getStrings(at));
        }
        else if (!permNeeded.isAuthorized(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You need to specify a reason!");
            return null;
        }
        return reason;
    }

    @Command(names = {"unban", "pardon"},
             desc = "Unbans a previously banned player.",
             min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        String userName = context.getString(0);
        if (!this.banManager.isUserBanned(userName))
        {
            context.sendTranslated(MessageType.NEGATIVE, "{user} is not banned, maybe you misspelled his name?", userName);
            return;
        }
        this.banManager.removeUserBan(userName);
        context.sendTranslated(MessageType.POSITIVE, "You unbanned {user}!", userName);
    }

    @Command(names = {"ipban", "banip"},
             desc = "Bans the IP from this server.",
             min = 1, max = 2, usage = "<IP address> [reason]")
    public void ipban(CommandContext context)
    {
        String ipaddress = context.getString(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipaddress);
            if (this.banManager.isIpBanned(address))
            {
                context.sendTranslated(MessageType.NEUTRAL, "The IP {input#ip} is already banned!", address.getHostAddress());
                return;
            }
            String reason = this.getReasonFrom(context,1, module.perms().COMMAND_IPBAN_NOREASON);
            if (reason == null) return;
            this.banManager.addBan(new IpBan(address,context.getSender().getName(), reason));
            context.sendTranslated(MessageType.NEGATIVE, "You banned the IP {input#ip} from your server!", address.getHostAddress());
            Set<String> bannedUsers = new HashSet<>();
            for (User user : um.getOnlineUsers())
            {
                if (user.getAddress() != null && user.getAddress().getAddress().getHostAddress().equals(ipaddress))
                {
                    user.kickPlayer(user.translate(MessageType.NEGATIVE, banMessage) + "\n" + ChatFormat.RESET + reason);
                    bannedUsers.add(user.getName());
                }
            }
            um.broadcastMessageWithPerm(MessageType.NEGATIVE, "The IP {input#ip} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, ipaddress, context.getSender());
            um.broadcastMessageWithPerm(MessageType.NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
            if (!bannedUsers.isEmpty())
            {
                um.broadcastMessageWithPerm(MessageType.NEGATIVE,"And with it kicked: {user#list}!", module.perms().BAN_RECEIVEMESSAGE,
                                            StringUtils.implode(ChatFormat.RED + "," + ChatFormat.DARK_GREEN, bannedUsers));
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(MessageType.NEGATIVE, "{input#ip} is not a valid IP-address!", ipaddress);
        }
    }

    @Command(names = {"ipunban", "unbanip", "pardonip"},
             desc = "Bans the IP from this server.",
             min = 1, max = 1, usage = "<IP address>")
    public void ipunban(CommandContext context)
    {
        String ipadress = context.getString(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipadress);
            this.banManager.removeIpBan(address);
            context.sendTranslated(MessageType.POSITIVE, "You unbanned the IP {input#ip}!", address.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(MessageType.NEGATIVE, "{input#ip} is not a valid IP-address!", ipadress);
        }
    }

    @Command(names = {"tempban","tban"},
             desc = "Bans a player for a given time.",
             min = 2, max = NO_MAX,
             usage = "<player> <time> [reason]",
             flags = @Flag(longName = "force", name = "f"))
    public void tempban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getName());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(MessageType.NEUTRAL, "{user} has never played on this server before! Use the -force flag to ban him anyways.", player);
            return;
        }
        String reason = this.getReasonFrom(context, 2, module.perms().COMMAND_TEMPBAN_NOREASON);
        if (reason == null) return;
        if (this.banManager.isUserBanned(player.getName()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "{user} is already banned!", player);
            return;
        }
        try
        {
            long millis = StringUtils.convertTimeToMillis(context.getString(1));
            Date toDate = new Date(System.currentTimeMillis() + millis);
            this.banManager.addBan(new UserBan(player.getName(),context.getSender().getName(), reason, toDate));
            if (player.isOnline())
            {
                if (user == null) throw new IllegalStateException();
                user.kickPlayer(user.translate(MessageType.NEGATIVE, banMessage) + "\n" + ChatFormat.RESET + reason);
            }
            context.sendTranslated(MessageType.POSITIVE, "You banned {user} temporarily!", player);
            um.broadcastMessageWithPerm(MessageType.NEGATIVE, "{user} was banned temporarily from the server by {sender}!\n{}", module.perms().BAN_RECEIVEMESSAGE, player, context.getSender(), reason);
        }
        catch (TimeConversionException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Invalid time value! Examples: 1d 12h 5m");
        }
    }

    private boolean cannotBanUser(CommandContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().commands.disallowBanIfOfflineMode)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Banning players by name is not allowed in offline-mode!");
                context.sendTranslated(MessageType.NEUTRAL, "You can change this in your Basics-Configuration.");
                return true;
            }
            context.sendTranslated(MessageType.NEUTRAL, "The server is running in {text:OFFLINE-mode:color=DARK_RED}.");
            context.sendTranslated(MessageType.NEUTRAL, "Players could change their username with a cracked client!");
            context.sendTranslated(MessageType.POSITIVE, "You can IP-ban to prevent banning a real player in that case.");
        }
        return false;
    }

    @Command(desc = "Reloads the ban lists")
    public void reloadbans(CommandContext context)
    {
        this.banManager.reloadBans();
        context.sendTranslated(MessageType.POSITIVE, "Reloaded the ban lists successfully!");
    }
}
