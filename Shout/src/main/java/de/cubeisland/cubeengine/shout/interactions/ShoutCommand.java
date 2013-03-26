package de.cubeisland.cubeengine.shout.interactions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.Announcement;

public class ShoutCommand
{
    private Shout module;

    public ShoutCommand(Shout module)
    {
        this.module = module;
    }

    @Command(names = {
        "shout", "announce"
    }, min = 1, max = 1, desc = "Announce a message to players on the server", usage = "<announcement name>")
    public void shout(CommandContext context)
    {
        Announcement announcement = this.module.getAnnouncementManager().getAnnouncement(context.getString(0));
        if (announcement == null)
        {
            context.sendMessage("shout", "&c%s was not found!", context.getString(0));
            return;
        }
        List<Player> players;

        if (announcement.getFirstWorld().equals("*"))
        {
            players = Arrays.asList(Bukkit.getOnlinePlayers());
        }
        else
        {
            players = Bukkit.getWorld(announcement.getFirstWorld()).getPlayers();
        }

        for (Player player : players)
        {
            User u = this.module.getCore().getUserManager().getExactUser(player);
            String[] message = announcement.getMessage(u.getLanguage());
            if (message != null)
            {
                for (String line : message)
                {
                    u.sendMessage(ChatFormat.parseFormats(line));
                }
            }
        }
        context.sendMessage("shout", "&aThe announcement &e%s&a has been announced!", announcement.getName());
    }
}
