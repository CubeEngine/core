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
package de.cubeisland.cubeengine.shout.interactions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.MessageOfTheDay;

public class ShoutSubCommands
{
    private Shout module;

    public ShoutSubCommands(Shout module)
    {
        this.module = module;
    }

    @Alias(names = {
        "announcements"
    })
    @Command(names = {
        "list", "announcements"
    }, desc = "List all announcements")
    public void list(CommandContext context)
    {
        Iterator<Announcement> iter = this.module.getAnnouncementManager().getDynamicAnnouncements().iterator();
        if (iter.hasNext())
        {
            context.sendTranslated("Here is the list of announcements:");
            while (iter.hasNext())
            {
                context.sendMessage(" - " + iter.next().getName());
            }
        }
        else
        {
            context.sendTranslated("There are no announcements loaded!");
        }
    }

    @Command(desc = "Create the structure for a new announcement", min = 1, max = 1, params = {
        @Param(names =
        {
            "delay", "d"
        }),
        @Param(names =
        {
            "world", "w"
        }),
        @Param(names = {
            "permission", "p"
        }, type = Permission.class),
        @Param(names =
        {
            "group", "g"
        }),
        @Param(names =
        {
            "message", "m"
        }),
        @Param(names =
        {
            "locale", "l"
        })
    }, flags = {
        @Flag(name = "fc", longName = "fixed-cycle")
    }, usage = "<name> message \"<message>\" [delay \"<x minutes|hours|days>\"] [world <world>] " +
            "[permission <permission node>] [group <group>] [locale <locale>] [-fixed-cycle]")
    public void create(ParameterizedContext context)
    {
        if (!context.hasParam("message"))
        {
            context.sendTranslated("You have to include a message!");
            return;
        }

        String message = context.getString("message");
        Locale locale = context.getSender().getLocale();
        if (context.hasParam("locale"))
        {
            locale = I18n.stringToLocale(context.getString("locale"));
        }
        if (locale == null)
        {
            context.sendTranslated("%s isn't a valid locale!", context.getString("locale"));
        }

        try
        {
            this.module.getAnnouncementManager().createAnnouncement(
                context.getString(0),
                locale,
                message,
                context.getString("delay", "10 minutes"),
                context.getString("world", "*"),
                context.getString("group", "*"),
                context.getString("permission", "*"),
                context.hasFlag("fc"));
        }
        catch (IllegalArgumentException ex)
        {
            context.sendTranslated("Some of your arguments are not valid.");
            context.sendTranslated("The error message was: %s", ex.getLocalizedMessage());
        }
        catch (IOException ex)
        {
            context.sendTranslated("There was an error creating some of the files.");
            context.sendTranslated("The error message was: %s", ex.getLocalizedMessage());
        }

        module.getAnnouncementManager().reload();

        context.sendTranslated("Your announcement have been created and loaded into the plugin");
    }

    @Command(desc = "clean all loaded announcements form memory and load from disk")
    public void reload(CommandContext context)
    {
        module.getAnnouncementManager().reload();
        context.sendTranslated("All players and announcements have now been reloaded");
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
            context.sendTranslated("&eThere is no message of the day.");
        }
    }
}
