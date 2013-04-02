package de.cubeisland.cubeengine.shout.interactions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import de.cubeisland.cubeengine.core.command.CommandContext;
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
        Iterator<Announcement> iter = this.module.getAnnouncementManager().getAnnouncements().iterator();
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
    }, usage = "<name> message \"<message>\" [delay \"<x minutes|hours|days>\"] [world <world>] " +
            "[permission <permission node>] [group <group>] [locale <locale>]")
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
                context.getString("permission", "*"));
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
