package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
        {
            return;
        }
        this.setLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            this.setLastAction((Player)event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerInteract(PlayerInteractEvent event)
    {
        this.setLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        User user = this.basics.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(this.basics, "lastAction", System.currentTimeMillis());
        this.run();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        this.setLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event)
    {
        this.setLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event)
    {
        User user = this.basics.getUserManager().getExactUser(event.getPlayer());
        user.removeAttribute(basics,"afk");
    }

    private void setLastAction(Player player)
    {
        User user = this.basics.getUserManager().getExactUser(player);
        Boolean isAfk = user.getAttribute(basics, "afk");
        if (isAfk != null && isAfk && BasicsPerm.AFK_PREVENT_AUTOUNAFK.isAuthorized(player))
        {
            return;
        }
        user.setAttribute(this.basics, "lastAction", System.currentTimeMillis());
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
