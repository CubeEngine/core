package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MuteListener implements Listener
{
    private final Basics basics;

    public MuteListener(Basics basics)
    {
        this.basics = basics;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!event.getMessage().startsWith("/"))
        {
            User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
            if (user != null)
            {
                BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
                if (bUser.muted != null && System.currentTimeMillis() > bUser.muted.getTime())
                {
                    event.setCancelled(true);
                    user.sendMessage("basics", "&cYou try to speak but nothing happens!");
                }
            }
        }
    }
}
