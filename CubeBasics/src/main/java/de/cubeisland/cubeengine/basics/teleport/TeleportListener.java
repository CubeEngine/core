package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener
{
    private Basics module;

    public TeleportListener(Basics module)
    {
        this.module = module;
    }

    public void onTeleport(PlayerTeleportEvent event)
    {
        User user = module.getUserManager().getUser(event.getPlayer());
        switch (event.getCause())
        {
            case COMMAND:
            case PLUGIN:
            case UNKNOWN:
                user.setAttribute("lastLocation", event.getFrom());
        }
    }
}
