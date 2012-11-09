package de.cubeisland.cubeengine.shout.interactions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.Announcement;

public class ShoutCommand
{
    private Shout module;

    public ShoutCommand(Shout module)
    {
        this.module = module;
    }

    @Command(names =
    {
        "shout", "announce"
    },
    min = 1,
    desc = "Announce a message to players on the server",
    usage = "<Announcment-name>")
    public void shout(CommandContext context)
    {
        try
        {
            if (module.getAnnouncementManager().hasAnnouncement(context.getIndexed(0, String.class)))
            {
                Announcement a = module.getAnnouncementManager().getAnnouncement(context.getIndexed(0, String.class));
                List<Player> players;

                if (a.getWorld().equals("*"))
                {
                    players = Arrays.asList(module.getCore().getServer().getOnlinePlayers());
                }
                else
                {
                    players = module.getCore().getServer().getWorld(a.getWorld()).getPlayers();
                }

                for (Player p : players)
                {
                    User u = module.getUserManager().getExactUser(p);
                    u.sendMessage(ChatFormat.parseFormats('&', a.getMessage(u.getLanguage())));
                }
                context.sendMessage("shout", "The announcement is announced");
            }
            else
            {
                context.sendMessage("shout", "%s is not an announcement...", context.getIndexed(0, String.class));
            }
        }
        catch (ConversionException ex)
        {
        } //This should never happen
    }
}
