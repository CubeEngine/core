package de.cubeisland.cubeengine.shout.interactions;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.Announcement;
import java.io.IOException;

public class ShoutSubCommands
{
    private Shout module;

    public ShoutSubCommands(Shout module)
    {
        this.module = module;
    }

    @Command(names =
    {
        "list", "announcements"
    },
    desc = "List all announcements")
    public void list(CommandContext context)
    {
        StringBuilder announcements = new StringBuilder();
        for (Announcement a : module.getAnnouncementManager().getAnnouncemets())
        {
            announcements.append(a.getName()).append(", ");
        }

        if (announcements.toString().isEmpty())
        {
            context.sendMessage("shoust", "There is no loaded announcements!");
            return;
        }
        context.sendMessage("shout", "Here is the list of announcements: %s",
            announcements.substring(0, announcements.length() - 2));
    }

    @Command(desc = "Create the structure for a new announcement",
    min = 1,
    params =
    {
        @Param(names =
        {
            "delay", "d"
        }, types = String.class),
        @Param(names =
        {
            "world", "w"
        }, types = String.class),
        @Param(names =
        {
            "permission", "p"
        }, types = Permission.class),
        @Param(names =
        {
            "group", "g"
        }, types = String.class),
        @Param(names =
        {
            "message", "m"
        }, types = String.class)
    })
    public void create(CommandContext context)
    {
        if (!context.hasNamed("message"))
        {
            context.sendMessage("shout", "You have to include a message!");
            return;
        }

        String message = "";
        for (Object o : context.getNamed("message"))
        {
            message += (String)o;
        }

        String locale = this.module.getCore().getConfiguration().defaultLanguage;
        if (context.getSenderAsUser() != null)
        {
            locale = context.getSenderAsUser().getLanguage();
        }

        try
        {
            this.module.getAnnouncementManager().createAnnouncement(
                context.getString(0),
                message,
                context.getNamed("delay", String.class, "10 minutes"),
                context.getNamed("world", String.class, "*"),
                context.getNamed("group", String.class, "*"),
                context.getNamed("permission", String.class, "*"),
                locale);
        }
        catch (IllegalArgumentException ex)
        {
            context.sendMessage("shout", "Some of your arguments are not valid.");
            context.sendMessage("shout", "The error message was: %S", ex.getLocalizedMessage());
        }
        catch (IOException ex)
        {
            context.sendMessage("shout", "There was an error creating some of the files.");
            context.sendMessage("shout", "The error message was: %S", ex.getLocalizedMessage());
        }

        module.getAnnouncementManager().clean();

        context.sendMessage("shout", "Your announcement have been created and loaded into the plugin");
    }

    @Command(
    desc = "clean all loaded announcements form memory and load from disk")
    public void reload(CommandContext context)
    {
        module.getAnnouncementManager().clean(); // TODO this doesn't seem like reloading or the method is named bad
        context.sendMessage("shout", "All players and announcements have now been reloaded");
    }
}
