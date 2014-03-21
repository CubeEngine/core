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
package de.cubeisland.engine.shout.interactions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.i18n.I18nUtil;
import de.cubeisland.engine.shout.Shout;
import de.cubeisland.engine.shout.announce.Announcement;
import de.cubeisland.engine.shout.announce.MessageOfTheDay;

public class ShoutCommand extends ContainerCommand
{
    private final Shout module;

    public ShoutCommand(Shout module)
    {
        super(module, "shout", "Announce a message to players on the server", Arrays.asList("announce"));
        this.module = module;

        this.setUsage("<announcement>");
        this.getContextFactory().setArgBounds(new ArgBounds(1, 1));
    }

    public CommandResult run(CommandContext context)
    {
        Announcement announcement = this.module.getAnnouncementManager().getAnnouncement(context.getString(0));
        if (announcement == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "{input#announcement} was not found!", context.getString(0));
            return null;
        }
        List<Player> players;

        if (announcement.getFirstWorld().equals("*"))
        {
            players = Arrays.asList(Bukkit.getOnlinePlayers());
        }
        else
        {
            players = new ArrayList<>();
            for (String world : announcement.getWorlds())
            {
                World w = Bukkit.getWorld(world);
                if (w != null)
                {
                    players.addAll(Bukkit.getWorld(world).getPlayers());
                }
            }
        }

        for (Player player : players)
        {
            User u = this.module.getCore().getUserManager().getExactUser(player.getName());
            String[] message = announcement.getMessage(u.getLocale());
            if (message != null)
            {
                u.sendMessage("");
                for (String line : message)
                {
                    u.sendMessage(ChatFormat.parseFormats(line));
                }
                u.sendMessage("");
            }
        }
        context.sendTranslated(MessageType.POSITIVE, "The announcement {name} has been announced!", announcement.getName());
        return null;
    }

    @Alias(names = {"announcements"})
    @Command(names = {"list", "announcements"}, desc = "List all announcements")
    public void list(CommandContext context)
    {
        Iterator<Announcement> iter = this.module.getAnnouncementManager().getAllAnnouncements().iterator();
        if (iter.hasNext())
        {
            context.sendTranslated(MessageType.POSITIVE, "Here is the list of announcements:");
            while (iter.hasNext())
            {
                context.sendMessage(" - " + iter.next().getName());
            }
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "There are no announcements loaded!");
        }
    }

    @Command(desc = "Creates a new announcement", min = 1, max = 1,
             params = {@Param(names ={"delay", "d"}),
                       @Param(names ={"world", "w"}),
                       @Param(names = {"permission", "p"}),
                       @Param(names ={"group", "g"}),
                       @Param(names ={"message", "m"}),
                       @Param(names ={"locale", "l"})},
             flags = {@Flag(name = "fc", longName = "fixed-cycle")},
             usage = "<name> message \"<message>\" [delay \"<x minutes|hours|days>\"] [world <world>] " +
                     "[permission <permission node>] [locale <locale>] [-fixed-cycle]")
    public void create(ParameterizedContext context)
    {
        if (!context.hasParam("message"))
        {
            context.sendTranslated(MessageType.NEUTRAL, "You have to include a message!");
            return;
        }

        String message = context.getString("message");
        Locale locale = context.getSender().getLocale();
        if (context.hasParam("locale"))
        {
            locale = I18nUtil.stringToLocale(context.getString("locale"));
        }
        if (locale == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "{input#locale} isn't a valid locale!", context.getString("locale"));
        }

        try
        {
            this.module.getAnnouncementManager().addAnnouncement(
                this.module.getAnnouncementManager().createAnnouncement(
                    context.getString(0),
                    locale,
                    message,
                    context.getString("delay", "10 minutes"),
                    context.getString("world", "*"),
                    context.getString("permission", "*"),
                    context.hasFlag("fc")));
        }
        catch (IllegalArgumentException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Some of your arguments are not valid.");
            context.sendTranslated(MessageType.NEGATIVE, "The error message was: {}", ex.getLocalizedMessage());
        }
        catch (IOException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "There was an error creating some of the files.");
            context.sendTranslated(MessageType.NEGATIVE, "The error message was: {}", ex.getLocalizedMessage());
        }

        module.getAnnouncementManager().reload();

        context.sendTranslated(MessageType.POSITIVE, "Your announcement have been created and loaded into the plugin");
    }

    @Command(desc = "clean all loaded announcements form memory and load from disk")
    public void reload(CommandContext context)
    {
        module.getAnnouncementManager().reload();
        context.sendTranslated(MessageType.POSITIVE, "All the announcements have now been reloaded, and the players have been re-added");
    }

    @Alias(names = "motd")
    @Command(desc = "Prints out the message of the day.")
    public void motd(CommandContext context)
    {
        MessageOfTheDay motd = this.module.getAnnouncementManager().getMotd();
        if (motd != null)
        {
            context.sendMessage(" ");
            for (String line : motd.getMessage(context.getSender().getLocale()))
            {
                context.sendMessage(line);
            }
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "There is no message of the day.");
        }
    }
}
