package de.cubeisland.cubeengine.basics.command.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

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
        String message = context.getStrings(1);
        if (message == null)
        {
            message = this.module.getConfiguration().defaultKickMessage;
        }
        message = ChatFormat.parseFormats(message);
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                context.sendMessage("basics", "&cYou are not allowed to kick everyone!");
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
            context.sendMessage("basics", "&cYou need to specify a player!");
            return;
        }
        User user = context.getUser(0);
        if (user == null && !context.hasFlag("a"))
        {
            context.sendMessage("basics", "&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        user.kickPlayer(message);
        context.getCore().getUserManager().broadcastMessage("basics", "&2%s &4was kicked from the server!", BasicsPerm.KICK_RECEIVEMESSAGE, user.getName());
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
                context.sendMessage("basics", "&cBanning players by name is not allowed in offline-mode!"
                        + "\n&eYou can change this in your Basics-Configuration.");
                return;
            }
            context.sendMessage("basics", "&eThe server is running in &4OFFLINE-mode&e. "
                    + "\nPlayers could change their username with a cracked client!\n"
                    + "&aYou can IP-ban to prevent banning a real player in that case.");
        }
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (player.isBanned())
        {
            context.sendMessage("basics", "&2%s &cis already banned!", player.getName());
            return;
        }
        if (player.hasPlayedBefore() == false)
        {
            context.sendMessage("basics", "&2%s &6has never played on this server before!", player.getName());
        }
        else if (context.hasFlag("ip"))
        {
            if (player.isOnline())
            {
                String ipadress = player.getPlayer().getAddress().getAddress().getHostAddress();
                Bukkit.banIP(ipadress);
                for (User ipPlayer : context.getCore().getUserManager().getOnlineUsers())
                {
                    if (!ipPlayer.getName().equals(player.getName())
                        && ipPlayer.getAddress() != null
                        && ipPlayer.getAddress().getAddress().getHostAddress().equals(ipadress))
                    {
                        ipPlayer.kickPlayer(_(ipPlayer, "basics", "&cYou were ip-banned from this server!"));
                    }
                }
                context.sendMessage("basics", "&cYou banned the IP: &e%s&c!", ipadress);
            }
            else
            {
                //TODO ip-ban when user is still loaded
                context.sendMessage("basics", "&eYou cannot IP-ban this player because he is offline!");
            }
        }
        if (player.isOnline())
        {
            User user = context.getCore().getUserManager().getExactUser(player);
            String message = context.getStrings(1);
            if (message.isEmpty())
            {
                message = _(user, "basics", "&cYou got banned from this server!");
            }
            else
            {
                message = ChatFormat.parseFormats(message);
            }
            user.kickPlayer(message);
        }
        player.setBanned(true);
        context.sendMessage("basics", "&cYou banned &2%s&c!", player.getName());
    }

    @Command(names = {
    "unban", "pardon"
    }, desc = "Unbans a previously banned player.", min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        OfflinePlayer offlinePlayer = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!offlinePlayer.isBanned())
        {
            context.sendMessage("basics", "&2%s &cis not banned!", offlinePlayer.getName());
            return;
        }
        offlinePlayer.setBanned(false);
        context.sendMessage("basics", "&aYou unbanned &2%s&a!", offlinePlayer.getName());
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
            context.sendMessage("basics", "&cYou banned the IP &6%s &cfrom your server!", adress.getHostAddress());
            for (User player : context.getCore().getUserManager().getOnlineUsers())
            {
                if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ipadress))
                {
                    player.kickPlayer(_(player, "basics", "&cYou were banned from this server!"));
                }
            }
        }
        catch (UnknownHostException e)
        {
            context.sendMessage("basics", "&6%s &cis not a valid IP-address!", ipadress);
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
            context.sendMessage("basics", "&aYou unbanned the IP &6%s&a!", adress.getHostAddress());
        }
        catch (UnknownHostException e)
        {
            context.sendMessage("basics", "&6%s &cis not a valid IP-address!", ipadress);
        }
    }
}
