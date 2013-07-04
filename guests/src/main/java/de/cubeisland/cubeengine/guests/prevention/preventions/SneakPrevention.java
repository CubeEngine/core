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
package de.cubeisland.cubeengine.guests.prevention.preventions;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;

/**
 * Prevents sneaking (the player still ducks, but the player's name above the
 * head stays visible as of Bukkit 1.1-R5-SNAPSHOT).
 */
public class SneakPrevention extends Prevention
{
    public SneakPrevention(Guests guests)
    {
        super("sneak", guests);
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\nThis prevention doesn't prevent crouching!\n";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void toggleSneak(PlayerToggleSneakEvent event)
    {
        final Player player = event.getPlayer();
        if (event.isSneaking())
        {
            if (!can(player))
            {
                if (!player.getGameMode().equals(GameMode.CREATIVE))
                {
                    sendMessage(player);
                    punish(player);
                }
                event.setCancelled(true);
            }
        }
    }
}
