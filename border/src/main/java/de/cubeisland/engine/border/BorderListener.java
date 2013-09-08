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
package de.cubeisland.engine.border;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.math.BlockVector2;

public class BorderListener implements Listener
{
    private final BorderConfig config;
    private final UserManager um;
    private Border module;

    public BorderListener(Border module)
    {
        this.module = module;
        this.config = module.config;
        this.um = module.getCore().getUserManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getChunk() == event.getTo().getChunk())
        {
            return;
        }
        if (this.config.allowBypass && BorderPerms.BYPASS.isAuthorized(event.getPlayer()))
        {
            return;
        }
        if (!this.isChunkInRange(event.getTo().getChunk()))
        {
            if (event instanceof PlayerTeleportEvent)
            {
                this.um.getExactUser(event.getPlayer().getName()).sendTranslated("&cYou cannot teleport outside the border!");
            }
            else
            {
                this.um.getExactUser(event.getPlayer().getName()).sendTranslated("&cYou've reached the border!");
            }
            event.setCancelled(true);
            event.setTo(event.getFrom().getWorld().getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        this.onPlayerMove(event);
    }

    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        final Chunk respawnChunk = event.getRespawnLocation().getChunk();
        if (!this.isChunkInRange(respawnChunk))
        {
            event.setRespawnLocation(respawnChunk.getWorld().getSpawnLocation());
        }
    }

    private boolean isChunkInRange(Chunk to)
    {
        final Chunk spawnChunk = to.getWorld().getSpawnLocation().getChunk();
        BlockVector2 spawnPos = new BlockVector2(spawnChunk.getX(), spawnChunk.getZ());
        return spawnPos.squaredDistance(new BlockVector2(to.getX(), to.getZ())) <= this.config.radius * this.config.radius;
    }

    // TODO prevent chunk generation behind the border
}
