package de.cubeisland.cubeengine.shout.interactions;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;

public class ShoutCommand
{
    private Shout module;

    public ShoutCommand(Shout module)
    {
        this.module = module;
    }

    @Command(
        names = {"shout", "announce"},
        min = 1,
        desc = "Announce a message to players on the server",
        usage = "<announcment name>"
    )
    public void shout(CommandContext context)
    {
        if (this.module.getAnnouncementManager().hasAnnouncement(context.getString(0)))
        {
            Announcement announcement = this.module.getAnnouncementManager().getAnnouncement(context.getString(0));
            List<Player> players;

            if (announcement.getWorld().equals("*"))
            {
                players = Arrays.asList(this.module.getCore().getServer().getOnlinePlayers());
            }
            else
            {
                players = this.module.getCore().getServer().getWorld(announcement.getWorld()).getPlayers();
            }

            for (Player player : players)
            {
                User u = this.module.getUserManager().getExactUser(player);
                u.sendMessage(ChatFormat.parseFormats('&', announcement.getMessage(u.getLanguage())));
            }
            context.sendMessage("shout", "The announcement is announced");
        }
        else
        {
            context.sendMessage("shout", "%s is not an announcement...", context.getString(0));
        }
    }
}
