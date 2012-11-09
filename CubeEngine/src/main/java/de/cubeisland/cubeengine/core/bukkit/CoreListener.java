package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.CubeEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CoreListener implements Listener
{
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        BukkitScheduler scheduler = CubeEngine.getCore().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask((Plugin)CubeEngine.getCore(), new Runnable()
        {
            @Override
            public void run()
            {
                AfterJoinEvent afterJoinEvent = new AfterJoinEvent(event.getPlayer(),event.getJoinMessage());
                CubeEngine.getEventManager().fireEvent(afterJoinEvent);
            }
        }, CubeEngine.getConfiguration().afterJoinEventDelay);
    }
}
