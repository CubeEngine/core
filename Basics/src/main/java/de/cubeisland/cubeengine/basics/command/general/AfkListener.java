package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AfkListener implements Listener, Runnable
{
    private final Basics basics;
    private final UserManager um;
    private final long autoAfk;
    private final long afkCheck;

    public AfkListener(Basics basics, long autoAfk, long afkCheck)
    {
        this.basics = basics;
        this.um = basics.getUserManager();
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
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            this.updateLastAction((Player)event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerInteract(PlayerInteractEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        this.updateLastAction(event.getPlayer());
        this.run();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event)
    {
        BasicsAttachment basicUser = this.um.getExactUser(event.getPlayer()).get(BasicsAttachment.class);
        if (basicUser != null)
        {
            basicUser.setAfk(false);
        }
    }

    private void updateLastAction(Player player)
    {
        BasicsAttachment basicUser = this.um.getExactUser(player).get(BasicsAttachment.class);
        if (basicUser != null)
        {
            if (basicUser.isAfk() && BasicsPerm.AFK_PREVENT_AUTOUNAFK.isAuthorized(player))
            {
                return;
            }
            basicUser.updateLastAction();
        }
    }

    @Override
    public void run()
    {
        BasicsAttachment basicUser;
        for (User user : basics.getUserManager().getLoadedUsers())
        {
            basicUser = user.get(BasicsAttachment.class);
            if (basicUser == null)
            {
                continue;
            }

            long lastAction = basicUser.getLastAction();
            if (lastAction == 0)
            {
                continue;
            }
            if (basicUser.isAfk())
            {
                if (System.currentTimeMillis() - lastAction < afkCheck)
                {
                    basicUser.setAfk(false);
                    this.um.broadcastStatus("basics", "is no longer afk!", user.getName());
                }
            }
            else if (System.currentTimeMillis() - lastAction > autoAfk)
            {
                basicUser.setAfk(true);
                this.um.broadcastStatus("basics", "is now afk!" ,user.getName());
            }
        }
    }
}
