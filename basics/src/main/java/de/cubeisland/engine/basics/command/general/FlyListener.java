/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.general;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.basics.Basics;

public class FlyListener implements Listener
{
    private final Basics module;

    public FlyListener(Basics module)
    {
        this.module = module;
    }

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
        catch (IllegalArgumentException ignored)
        {}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void join(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.CREATIVE && wasFlying(player) && module.perms().COMMAND_FLY_KEEP.isAuthorized(player))
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
        if (player.getGameMode() != GameMode.CREATIVE && player.isFlying() && module.perms().COMMAND_FLY_KEEP.isAuthorized(player))
        {
            markFlySpeed(player);
        }
    }
}
