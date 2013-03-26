package de.cubeisland.cubeengine.basics.command.general;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.storage.BasicUser;

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
            // muted?
            User sender = CubeEngine.getUserManager().getExactUser(event.getPlayer());
            if (sender != null)
            {
                BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(sender);
                if (bUser.muted != null && System.currentTimeMillis() < bUser.muted.getTime())
                {
                    event.setCancelled(true);
                    sender.sendMessage("basics", "&cYou try to speak but nothing happens!");
                }
            }
            // ignored?
            ArrayList<Player> ignore = new ArrayList<Player>();
            for (Player player : event.getRecipients())
            {
                User user = this.basics.getCore().getUserManager().getExactUser(player);
                if (this.basics.getIgnoreListManager().checkIgnore(user, sender))
                {
                    ignore.add(player);
                }
            }
            event.getRecipients().removeAll(ignore);
        }
    }
}
