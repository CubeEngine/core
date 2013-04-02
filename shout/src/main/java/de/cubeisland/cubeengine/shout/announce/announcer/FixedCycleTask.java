package de.cubeisland.cubeengine.shout.announce.announcer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.Announcement;

public class FixedCycleTask implements Runnable
{
    private final Shout module;
    private final Announcement announcement;

    public FixedCycleTask(Shout module, Announcement announcement)
    {
        this.module = module;
        this.announcement = announcement;
    }


    @Override
    public void run()
    {
        if (announcement.getFirstWorld().equals("*"))
        {
            for (User user : module.getCore().getUserManager().getOnlineUsers())
            {
                user.sendMessage(announcement.getMessage(user.getLocale()));
            }
        }
        else
        {
            for (String world : announcement.getWorlds())
            {
                for (Player player : Bukkit.getWorld(world).getPlayers())
                {
                    User user = module.getCore().getUserManager().getUser(player);
                    user.sendMessage(announcement.getMessage(user.getLocale()));
                }
            }
        }
    }
}
