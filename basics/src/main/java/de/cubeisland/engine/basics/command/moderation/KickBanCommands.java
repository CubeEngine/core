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
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.readers.UserOrAllReader;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.McUUID;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.TimeConversionException;

import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

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

    private static final String kickMessage = "You have been kicked from the server!";
    private static final String banMessage = "You have been banned from this server!";

    public KickBanCommands(Basics module)
    {
        this.module = module;
        this.banManager = this.module.getCore().getBanManager();
        this.um = this.module.getCore().getUserManager();
    }

    @Command(desc = "Kicks a player from the server")
    @IParams({@Grouped(@Indexed(label = {"player","!*"}, type = UserOrAllReader.class)),
              @Grouped(value = @Indexed(label = "reason"), req = false, greedy = true)})
    public void kick(ParameterizedContext context)
    {
        String reason;
        reason = this.getReasonFrom(context, 1, module.perms().COMMAND_KICK_NOREASON);
        if (reason == null) return;
        if ("*".equals(context.getArg(0)))
        {
            if (!module.perms().COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to kick everyone!");
                return;
            }
            for (User toKick : this.um.getOnlineUsers())
            {
                if (!context.getSender().equals(toKick))
                {
                    toKick.kickPlayer(toKick.getTranslation(NEGATIVE, kickMessage) + "\n\n" + RESET + reason);
                }
            }
            return;
        }
        User user = context.getArg(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        user.kickPlayer(user.getTranslation(NEGATIVE, kickMessage) + "\n\n" + RESET + reason);
        this.um.broadcastMessageWithPerm(NEGATIVE, "{user} was kicked from the server by {user}!", module.perms().KICK_RECEIVEMESSAGE, user, context.getSender());
        this.um.broadcastMessageWithPerm(NONE, reason, module.perms().KICK_RECEIVEMESSAGE);
    }

    @Command(alias = "kickban", desc = "Bans a player permanently on your server.")
    @IParams({@Grouped(@Indexed(label = "player", type = OfflinePlayer.class)),
              @Grouped(value = @Indexed(label = "reason"), req = false, greedy = true)})
    @Flags({@Flag(longName = "ipban", name = "ip"),
            @Flag(longName = "force", name = "f")})
    public void ban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getArg(0);
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getUniqueId());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(NEGATIVE,"{user} has never played on this server before! Use the -force flag to ban him anyway.", player);
            return;
        }
        String reason = this.getReasonFrom(context, 1, module.perms().COMMAND_BAN_NOREASON);
        if (reason == null) return;
        if (context.hasFlag("ip"))
        {
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "You cannot IP ban a player that has never played on the server before!");
                return;
            }
            if (user.getAddress() != null)
            {
                InetAddress ipAdress = user.getAddress().getAddress();
                if (this.banManager.isIpBanned(ipAdress))
                {
                    context.sendTranslated(NEGATIVE, "{user} is already IP banned!", player);
                    return;
                }
                this.banManager.addBan(new IpBan(ipAdress,context.getSender().getName(), reason));
                Set<String> bannedUsers = new HashSet<>();
                for (User ipPlayer : um.getOnlineUsers())
                {
                    if (ipPlayer.getAddress() != null && ipPlayer.getAddress().getAddress().equals(ipAdress))
                    {
                        ipPlayer.kickPlayer(ipPlayer.getTranslation(NEGATIVE, banMessage) + "\n\n" + RESET + reason);
                        bannedUsers.add(ipPlayer.getName());
                    }
                }
                context.sendTranslated(NEGATIVE, "You banned the IP: {input#ip}!", ipAdress.getHostAddress());
                um.broadcastMessageWithPerm(NEGATIVE, "{user} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, user, context.getSender());
                um.broadcastMessageWithPerm(NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
                um.broadcastMessageWithPerm(NEGATIVE, "And with it kicked: {user#list}!", module.perms().BAN_RECEIVEMESSAGE,
                                            StringUtils.implode(RED + "," + DARK_GREEN, bannedUsers));
            }
            else
            {
                context.sendTranslated(NEUTRAL, "You cannot IP ban {user} because he was offline for too long!", player);
            }
            return;
        }
        else
        {
            if (this.banManager.isUserBanned(player.getUniqueId()))
            {
                context.sendTranslated(NEGATIVE, "{user} is already banned!", player);
                return;
            }
            this.banManager.addBan(new UserBan(player.getName(), context.getSender().getName(), reason));
            if (user != null && user.isOnline())
            {
                user.kickPlayer(user.getTranslation(NEGATIVE, banMessage) + "\n\n" + RESET + reason);
            }
        }
        context.sendTranslated(NEGATIVE, "You banned {user}!", player);
        um.broadcastMessageWithPerm(NEGATIVE, "{user} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, player, context.getSender());
        um.broadcastMessageWithPerm(NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
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
            context.sendTranslated(NEGATIVE, "You need to specify a reason!");
            return null;
        }
        return reason;
    }

    @Command(alias = "pardon", desc = "Unbans a previously banned player.")
    @IParams(@Grouped(@Indexed(label = "player")))
    public void unban(CommandContext context)
    {
        String userName = context.getArg(0);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(userName);
        if (offlinePlayer.getUniqueId().version() == 3)
        {
            offlinePlayer = Bukkit.getOfflinePlayer(McUUID.getUUIDForName(userName));
        }
        if (this.banManager.removeUserBan(offlinePlayer.getUniqueId()))
        {
            context.sendTranslated(POSITIVE, "You unbanned {user}({name#uuid})!", userName, offlinePlayer.getUniqueId().toString());
            return;
        }
        context.sendTranslated(NEGATIVE, "{user} is not banned, maybe you misspelled his name?", userName);
    }

    @Command(alias = "banip", desc = "Bans the IP from this server.")
    @IParams({@Grouped(@Indexed(label = "IP address")),
              @Grouped(value = @Indexed(label = "reason"), req = false, greedy = true)})
    public void ipban(CommandContext context)
    {
        String ipaddress = context.getArg(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipaddress);
            if (this.banManager.isIpBanned(address))
            {
                context.sendTranslated(NEUTRAL, "The IP {input#ip} is already banned!", address.getHostAddress());
                return;
            }
            String reason = this.getReasonFrom(context,1, module.perms().COMMAND_IPBAN_NOREASON);
            if (reason == null) return;
            this.banManager.addBan(new IpBan(address,context.getSender().getName(), reason));
            context.sendTranslated(NEGATIVE, "You banned the IP {input#ip} from your server!", address.getHostAddress());
            Set<String> bannedUsers = new HashSet<>();
            for (User user : um.getOnlineUsers())
            {
                if (user.getAddress() != null && user.getAddress().getAddress().getHostAddress().equals(ipaddress))
                {
                    user.kickPlayer(user.getTranslation(NEGATIVE, banMessage) + "\n\n" + RESET + reason);
                    bannedUsers.add(user.getName());
                }
            }
            um.broadcastMessageWithPerm(NEGATIVE, "The IP {input#ip} was banned from the server by {sender}!", module.perms().BAN_RECEIVEMESSAGE, ipaddress, context.getSender());
            um.broadcastMessageWithPerm(NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
            if (!bannedUsers.isEmpty())
            {
                um.broadcastMessageWithPerm(NEGATIVE,"And with it kicked: {user#list}!", module.perms().BAN_RECEIVEMESSAGE,
                                            StringUtils.implode(RED + "," + DARK_GREEN, bannedUsers));
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(NEGATIVE, "{input#ip} is not a valid IP address!", ipaddress);
        }
    }

    @Command(alias = {"unbanip", "pardonip"}, desc = "Bans the IP from this server.")
    @IParams(@Grouped(@Indexed(label = "IP address")))
    public void ipunban(CommandContext context)
    {
        String ipadress = context.getArg(0);
        try
        {
            InetAddress address = InetAddress.getByName(ipadress);
            if (this.banManager.removeIpBan(address))
            {
                context.sendTranslated(POSITIVE, "You unbanned the IP {input#ip}!", address.getHostAddress());
            }
            else
            {
                context.sendTranslated(NEGATIVE, "The IP {input#ip} was not banned!", address.getHostAddress());
            }
        }
        catch (UnknownHostException e)
        {
            context.sendTranslated(NEGATIVE, "{input#ip} is not a valid IP address!", ipadress);
        }
    }

    @Command(alias = "tban", desc = "Bans a player for a given time.")
    @IParams({@Grouped(@Indexed(label = "player", type = OfflinePlayer.class)),
              @Grouped(@Indexed(label = "time")),
              @Grouped(value = @Indexed(label = "reason"), req = false, greedy = true)})
    @Flags(@Flag(longName = "force", name = "f"))
    public void tempban(ParameterizedContext context)
    {
        if (this.cannotBanUser(context)) return;
        OfflinePlayer player = context.getArg(0);
        User user = null;
        if (player.hasPlayedBefore() || player.isOnline())
        {
            user = um.getExactUser(player.getName());
        }
        else if (!context.hasFlag("f"))
        {
            context.sendTranslated(NEUTRAL, "{user} has never played on this server before! Use the -force flag to ban him anyways.", player);
            return;
        }
        String reason = this.getReasonFrom(context, 2, module.perms().COMMAND_TEMPBAN_NOREASON);
        if (reason == null) return;
        if (this.banManager.isUserBanned(player.getUniqueId()))
        {
            context.sendTranslated(NEGATIVE, "{user} is already banned!", player);
            return;
        }
        try
        {
            long millis = StringUtils.convertTimeToMillis(context.<String>getArg(1));
            Date toDate = new Date(System.currentTimeMillis() + millis);
            this.banManager.addBan(new UserBan(player.getName(),context.getSender().getName(), reason, toDate));
            if (player.isOnline())
            {
                if (user == null) throw new IllegalStateException();
                user.kickPlayer(user.getTranslation(NEGATIVE, banMessage) + "\n\n" + RESET + reason);
            }
            context.sendTranslated(POSITIVE, "You banned {user} temporarily!", player);
            um.broadcastMessageWithPerm(NEGATIVE, "{user} was banned temporarily from the server by {sender}!",
                                        module.perms().BAN_RECEIVEMESSAGE, player, context.getSender());
            um.broadcastMessageWithPerm(NONE, reason, module.perms().BAN_RECEIVEMESSAGE);
        }
        catch (TimeConversionException ex)
        {
            context.sendTranslated(NEGATIVE, "Invalid time value! Examples: 1d 12h 5m");
        }
    }

    private boolean cannotBanUser(CommandContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().commands.disallowBanIfOfflineMode)
            {
                context.sendTranslated(NEGATIVE, "Banning players by name is not allowed in offline-mode!");
                context.sendTranslated(NEUTRAL, "You can change this in your Basics-Configuration.");
                return true;
            }
            context.sendTranslated(NEUTRAL, "The server is running in {text:OFFLINE-mode:color=DARK_RED}.");
            context.sendTranslated(NEUTRAL, "Players could change their username with a cracked client!");
            context.sendTranslated(POSITIVE, "You can IP-ban to prevent banning a real player in that case.");
        }
        return false;
    }

    @Command(desc = "Reloads the ban lists")
    public void reloadbans(CommandContext context)
    {
        this.banManager.reloadBans();
        context.sendTranslated(POSITIVE, "Reloadhe ban lists successfully!");
    }

    @Command(desc = "View all players banned from this server")
    @IParams(@Grouped(req = false, value = @Indexed(label = {"!ips","!players"})))
    public void banlist(CommandContext context)
    {
        // TODO paging
        boolean players = true;
        if (context.hasArg(0))
        {
            if ("players".equalsIgnoreCase(context.<String>getArg(0)))
            {
                players = true;
            }
            else if ("ips".equalsIgnoreCase(context.<String>getArg(0)))
            {
                players = false;
            }
            else
            {
                context.sendTranslated(NEUTRAL, context.getCommand().getUsage(context));
            }
        }
        if (players)
        {
            Set<UserBan> userBans = this.banManager.getUserBans();
            if (userBans.isEmpty())
            {
                context.sendTranslated(POSITIVE, "There are no players banned on this server!");
                return;
            }
            if (userBans.size() == 1)
            {
                UserBan next = userBans.iterator().next();
                context.sendTranslated(POSITIVE, "Only {user}({name#uuid}) is banned on this server", um.getExactUser(next.getTarget()).getDisplayName(), next.getTarget());
                return;
            }
            context.sendTranslated(POSITIVE, "The following {amount} players are banned from this server", userBans.size());
            context.sendMessage(StringUtils.implode(GREY + ", ", userBans));
            return;
        }
        Set<IpBan> ipbans = this.banManager.getIpBans();
        if (ipbans.isEmpty())
        {
            context.sendTranslated(POSITIVE, "There are no IPs banned on this server!");
            return;
        }
        if (ipbans.size() == 1)
        {
            IpBan next = ipbans.iterator().next();
            context.sendTranslated(POSITIVE, "Only the IP {name#ip} is banned on this server", next.getTarget().getHostAddress());
            return;
        }
        context.sendTranslated(POSITIVE, "The following {amount} IPs are banned from this server", ipbans.size());
        context.sendMessage(StringUtils.implode(GREY + ", ", ipbans));
    }
}
