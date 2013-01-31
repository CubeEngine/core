package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
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

    public KickBanCommands(Basics module) {
        this.module = module;
    }

    @Command(desc = "Kicks a player from the server", usage = "<<player>|-all> [message]", flags = {
        @Flag(longName = "all", name = "a")
    })
    public void kick(CommandContext context)
    {
        String message = context.getStrings(1);
        if (message.isEmpty())
        {
            message = "Kicked!";
        }
        if (!context.hasFlag("a"))
        {
            if (!context.hasIndexed(0))
            {
                blockCommand(context, "basics", "&cYou need to specify a player!");
            }
            User user = context.getUser(0);
            if (user == null && !context.hasFlag("a"))
            {
                paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
            }
            user.kickPlayer(message);
            context.getCore().getUserManager().broadcastMessage("basics", "&2%s &4was kicked from the server!", user.getName());
        }
        else
        {
            if (!BasicsPerm.COMMAND_KICK_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to kick everyone!");
            }
            String sendername = context.getSender().getName();
            for (Player player : context.getSender().getServer().getOnlinePlayers())
            {
                if (!sendername.equalsIgnoreCase(player.getName()))
                {
                    player.kickPlayer(message);
                }
            }
        }
    }

    @Command(names = {
        "ban", "kickban"
    }, desc = "Bans a player permanently on your server.", min = 1, usage = "<player> [message] [-ipban]", flags = {
        @Flag(longName = "ipban", name = "ip")
    })
    public void ban(CommandContext context)
    {
        if (!Bukkit.getOnlineMode())
        {
            if (this.module.getConfiguration().disallowBanIfOfflineMode)
            {
                context.sendMessage("basics", "&cBanning players by name is not allowed in offline-mode!"
                        + "\n&eYou can change this in your Basics-Configuration.");
                return;
            }
            context.sendMessage("basics", "&eThe server is running in &cOFFLINE-mode&e. "
                    + "\nPlayers could change their username with a cracked client!\n"
                    + "&aYou can IP-ban to prevent banning a real player in that case.");
        }
        OfflinePlayer player = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (player.isBanned())
        {
            blockCommand(context, "basics", "&2%s &cis already banned!", player.getName());
        }
        if (player.hasPlayedBefore() == false)
        {
            context.sendMessage("basics", "&2%s &6has never played on this server before!", player.getName());
        }
        else
        {
            if (context.hasFlag("ip"))
            {
                if (player.isOnline())
                {
                    String ipadress = player.getPlayer().getAddress().getAddress().getHostAddress();
                    Bukkit.banIP(ipadress);
                    for (Player ipPlayer : context.getCore().getUserManager().getOnlinePlayers())
                    {
                        if (!ipPlayer.getName().equals(player.getName()))
                        {
                            if (ipPlayer.getAddress() != null && ipPlayer.getAddress().getAddress().getHostAddress().equals(ipadress))
                            {
                                ipPlayer.kickPlayer(_(ipPlayer, "basics", "&cYou were ip-banned from this server!"));
                            }
                        }
                    }
                    context.sendMessage("basics", "&cYou banned the IP: &e%s&c!", ipadress);
                }
                else
                {
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
                user.kickPlayer(message);
            }
        }
        player.setBanned(true);
        context.sendMessage("basics", "&cYou banned &2%s&c!", player.getName());
    }

    @Command(names = {
        "unban", "pardon"
    }, desc = "Unbans a previously banned player.", min = 1, max = 1, usage = "<player>")
    public void unban(CommandContext context)
    {
        OfflinePlayer user = context.getSender().getServer().getOfflinePlayer(context.getString(0));
        if (!user.isBanned())
        {
            blockCommand(context, "basics", "&2%s &cis not banned!", user.getName());
        }
        user.setBanned(false);
        context.sendMessage("basics", "&aYou unbanned &2%s&a!", user.getName());
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
            for (Player player : context.getCore().getUserManager().getOnlinePlayers())
            {
                if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(ipadress))
                {
                    player.kickPlayer(_(player, "basics", "&cYou were banned from this server!"));
                }
            }
        }
        catch (UnknownHostException e)
        {
            paramNotFound(context, "basics", "&6%s &cis not a valid IP-address!", ipadress);
        }
    }

    @Command(names = {
        "ipunban", "unbanip", "pardonip"
    }, desc = "Bans the IP from this server.", min = 1, usage = "<IP address>")
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
            paramNotFound(context, "basics", "&6%s &cis not a valid IP-address!", ipadress);
        }
    }
}
