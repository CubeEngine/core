package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.CorePerms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PreventSpamKickListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event)
    {
        if (event.getReason().equals("disconnect.spam") && CorePerms.SPAM.isAuthorized(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }
}
