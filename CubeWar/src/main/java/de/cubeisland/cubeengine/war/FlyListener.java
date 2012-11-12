package de.cubeisland.cubeengine.war;

import de.cubeisland.cubeengine.fly.FlyStartEvent;
import de.cubeisland.cubeengine.war.user.PvP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FlyListener implements Listener
{
    @EventHandler
    public void onFlyStart(final FlyStartEvent event)
    {
        if (PvP.isFlyBlocked(event.getUser()))
        {
            event.setCancelled(true);
        }
    }
}
