package de.cubeisland.cubeengine.shout.interactions;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import de.cubeisland.cubeengine.shout.announce.Announcer;
import de.cubeisland.cubeengine.shout.announce.MessageTask;
import de.cubeisland.cubeengine.shout.announce.UserReceiver;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShoutListener implements Listener
{
    private Shout module;
    private AnnouncementManager am;
    private Announcer announcner;

    public ShoutListener(Shout module)
    {
        this.module = module;
        this.am = module.getAnnouncementManager();
        this.announcner = module.getTaskManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent(PlayerJoinEvent event)
    {
        User user = this.module.getUserManager().getExactUser(event.getPlayer());

        if (this.module.getCore().isDebug())
        {
            this.module.getLogger().log(LogLevel.DEBUG, "Loading user: {0}", user.getName());
        }
        this.am.initializeReceiver(new UserReceiver(user, am));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerLeaveEvent(PlayerQuitEvent event)
    {
        this.am.clean(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        this.am.setWorld(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
    }
}
