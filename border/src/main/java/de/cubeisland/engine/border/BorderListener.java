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
import org.bukkit.Location;
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
                event.setCancelled(true);
                return;
            }
            if (this.module.getConfig().torusWorld)
            {
                Location subtract = event.getFrom().getWorld().getSpawnLocation().subtract(event.getFrom());
                Location torusLoc = event.getFrom().getWorld().getSpawnLocation().add(subtract);
                torusLoc = torusLoc.subtract(subtract.multiply(23 / subtract.length())); // adjust ~1 chunk diagonal closer to spawn
                torusLoc.setY(torusLoc.getWorld().getHighestBlockYAt(torusLoc));
                torusLoc.setYaw(event.getFrom().getYaw());
                torusLoc.setPitch(event.getFrom().getPitch());
                event.setTo(torusLoc); // move to torus loc
                this.um.getExactUser(event.getPlayer().getName()).sendTranslated("&eAs you reach the border the world turns on itself and you appear on the other side of the world!");
            }
            else
            {
                event.setTo(event.getFrom()); // no movement!
                this.um.getExactUser(event.getPlayer().getName()).sendTranslated("&cYou've reached the border!");
            }
        }
        else if (!(event instanceof PlayerTeleportEvent) && this.isChunkAlmostOutOfRange(event.getTo().getChunk()))
        {
            this.um.getExactUser(event.getPlayer().getName()).sendTranslated("&6You are near the world-border! You might want to turn around.");
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

    private boolean isChunkAlmostOutOfRange(Chunk to)
    {
        final Chunk spawnChunk = to.getWorld().getSpawnLocation().getChunk();
        BlockVector2 spawnPos = new BlockVector2(spawnChunk.getX(), spawnChunk.getZ());
        return !(spawnPos.squaredDistance(new BlockVector2(to.getX(), to.getZ())) <= (this.config.radius -2) * (this.config.radius -2));
    }

    // TODO prevent chunk generation behind the border, not possible with Bukkit atm
}
