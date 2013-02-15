package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener, Runnable
{
    private Basics basics;
    private long autoAfk;
    private long afkCheck;

    public AfkListener(Basics basics, long autoAfk, long afkCheck)
    {
        this.basics = basics;
        this.autoAfk = autoAfk;
        this.afkCheck = afkCheck;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
        {
            return;
        }
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            User user = CubeEngine.getUserManager().getExactUser((Player)event.getWhoClicked());
            user.setAttribute(basics, "lastAction", System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void playerInteract(PlayerInteractEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }

    @Override
    public void run()
    {
        for (User user : basics.getUserManager().getLoadedUsers())
        {
            Boolean isAfk = user.getAttribute(basics, "afk");
            Long lastAction = user.getAttribute(basics, "lastAction");
            if (lastAction == null)
            {
                continue;
            }
            if (isAfk != null && isAfk)
            {
                if (System.currentTimeMillis() - lastAction < afkCheck)
                {
                    user.removeAttribute(basics, "afk");
                    basics.getUserManager().broadcastStatus("basics", "is no longer afk!", user.getName());
                }
            }
            else if (System.currentTimeMillis() - lastAction > autoAfk)
            {
                user.setAttribute(basics, "afk", true);
                basics.getUserManager().broadcastStatus("basics", "is now afk!" ,user.getName());
            }
        }
    }
}
