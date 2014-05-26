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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.NParams;
import de.cubeisland.engine.core.command.reflected.context.Named;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.i18n.I18nUtil;
import de.cubeisland.engine.shout.Shout;
import de.cubeisland.engine.shout.announce.Announcement;
import de.cubeisland.engine.shout.announce.MessageOfTheDay;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.util.Arrays.asList;

public class ShoutCommand extends ContainerCommand
{
    private final Shout module;

    public ShoutCommand(Shout module)
    {
        super(module, "shout", "Announce a message to players on the server");
        this.setAliases(new HashSet<>(asList("announce")));
        this.module = module;

        // TODO this.setUsage("<announcement>");
        // TODO this.getContextFactory().setArgBounds(new ArgBounds(1, 1));
    }

    public CommandResult run(CommandContext context)
    {
        Announcement announcement = this.module.getAnnouncementManager().getAnnouncement(context.<String>getArg(0));
        if (announcement == null)
        {
            context.sendTranslated(NEGATIVE, "{input#announcement} was not found!", context.getArg(0));
            return null;
        }
        List<Player> players;

        if (announcement.getFirstWorld().equals("*"))
        {
            players = asList(Bukkit.getOnlinePlayers());
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
            User u = this.module.getCore().getUserManager().getExactUser(player.getUniqueId());
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
        context.sendTranslated(POSITIVE, "The announcement {name} has been announced!", announcement.getName());
        return null;
    }

    @Alias(names = {"announcements"})
    @Command(alias = "announcements", desc = "List all announcements")
    public void list(CommandContext context)
    {
        Iterator<Announcement> iter = this.module.getAnnouncementManager().getAllAnnouncements().iterator();
        if (iter.hasNext())
        {
            context.sendTranslated(POSITIVE, "Here is the list of announcements:");
            while (iter.hasNext())
            {
                context.sendMessage(" - " + iter.next().getName());
            }
        }
        else
        {
            context.sendTranslated(NEGATIVE, "There are no announcements loaded!");
        }
    }

    @Command(desc = "Creates a new announcement")
    @IParams(@Grouped(@Indexed(label = "name")))
    @NParams({@Named(names ={"message", "m"}),
              @Named(names ={"delay", "d"}, label = "<x> minutes|hours|days"),
              @Named(names ={"world", "w"}),
              @Named(names = {"permission", "p"}, label = "permission node"),
              @Named(names ={"group", "g"}),
              @Named(names ={"locale", "l"})})
    @Flags(@Flag(name = "fc", longName = "fixed-cycle"))
    public void create(ParameterizedContext context)
    {
        if (!context.hasParam("message"))
        {
            context.sendTranslated(NEUTRAL, "You have to include a message!");
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
            context.sendTranslated(NEGATIVE, "{input#locale} isn't a valid locale!", context.getString("locale"));
        }

        try
        {
            this.module.getAnnouncementManager().addAnnouncement(
                this.module.getAnnouncementManager().createAnnouncement(
                    context.<String>getArg(0),
                    locale,
                    message,
                    context.getString("delay", "10 minutes"),
                    context.getString("world", "*"),
                    context.getString("permission", "*"),
                    context.hasFlag("fc")));
        }
        catch (IllegalArgumentException ex)
        {
            context.sendTranslated(NEGATIVE, "Some of your arguments are not valid.");
            context.sendTranslated(NEGATIVE, "The error message was: {}", ex.getLocalizedMessage());
        }
        catch (IOException ex)
        {
            context.sendTranslated(NEGATIVE, "There was an error creating some of the files.");
            context.sendTranslated(NEGATIVE, "The error message was: {}", ex.getLocalizedMessage());
        }

        module.getAnnouncementManager().reload();

        context.sendTranslated(POSITIVE, "Your announcement have been created and loaded into the plugin");
    }

    @Command(desc = "clean all loaded announcements form memory and load from disk")
    public void reload(CommandContext context)
    {
        module.getAnnouncementManager().reload();
        context.sendTranslated(POSITIVE, "All the announcements have now been reloaded, and the players have been re-added");
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
            context.sendTranslated(NEUTRAL, "There is no message of the day.");
        }
    }
}
