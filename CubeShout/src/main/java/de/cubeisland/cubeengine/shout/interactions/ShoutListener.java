package de.cubeisland.cubeengine.shout.interactions;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.Announcer;
import de.cubeisland.cubeengine.shout.task.MessageTask;
import java.util.logging.Level;
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
            this.module.getLogger().log(Level.INFO, "Loading user: {0}", user.getName());
        }
        this.am.initializeUser(user);

        if (module.getCore().isDebug())
        {
            this.module.getLogger().log(Level.INFO, String.format("Scheduling a task for: %s every %d ticks.", user.getName(), this.am.getGreatestCommonDivisor(user.getName())));
        }
        this.announcner.scheduleTask(user.getName(), new MessageTask(am, module.getTaskManger(), user), this.am.getGreatestCommonDivisor(user.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerLeaveEvent(PlayerQuitEvent event)
    {
        this.announcner.stopUser(event.getPlayer().getName());
        this.am.clean(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        this.am.setWorld(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
    }
}
