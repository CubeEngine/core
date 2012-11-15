package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener
{
    private Basics basics;

    public TeleportListener(Basics basics)
    {
        this.basics = basics;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        User user = basics.getUserManager().getExactUser(event.getPlayer());
        switch (event.getCause()) {
            case COMMAND:
            case PLUGIN:
            case UNKNOWN:
                user.setAttribute(basics, "lastLocation", event.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        User user = this.basics.getUserManager().getExactUser(event.getEntity());
        if (BasicsPerm.COMMAND_BACK_ONDEATH.isAuthorized(user))
        {
            user.setAttribute(basics, "lastLocation", user.getLocation());
        }
    }
}
