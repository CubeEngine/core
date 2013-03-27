package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FlyListener implements Listener
{
    private static final float FLY_SPEED_MARKER = 42.0f;
    private static final float FLY_SPEED_DETECT = -10.0f;
    private static final float FLY_SPEED_DEFAULT = 0.05f;

    private static boolean wasFlying(Player p)
    {
        return p.getFlySpeed() < FLY_SPEED_DETECT;
    }

    private static void resetFlySpeed(Player p)
    {
        try
        {
            p.setFlySpeed(p.getFlySpeed() + FLY_SPEED_MARKER * 2f);
        }
        catch (IllegalArgumentException e)
        {
            p.setFlySpeed(FLY_SPEED_DEFAULT);
        }
    }

    private static void markFlySpeed(Player p)
    {
        if (wasFlying(p))
        {
            // already marked
            return;
        }
        try
        {
            ((CraftPlayer)p).getHandle().abilities.flySpeed -= FLY_SPEED_MARKER;
        }
        catch (IllegalArgumentException e)
        {}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void join(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.CREATIVE && wasFlying(player) && BasicsPerm.COMMAND_FLY_KEEP.isAuthorized(player))
        {
            player.setAllowFlight(true);
            player.setFlying(true);
            resetFlySpeed(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void quit(final PlayerQuitEvent event)
    {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.isFlying() && BasicsPerm.COMMAND_FLY_KEEP.isAuthorized(player))
        {
            markFlySpeed(player);
        }
    }
}
