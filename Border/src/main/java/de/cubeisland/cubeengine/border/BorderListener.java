package de.cubeisland.cubeengine.border;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.math.BlockVector2;

public class BorderListener implements Listener
{
    private final BorderConfig config;
    private final UserManager um;

    public BorderListener(Border border)
    {
        this.config = border.config;
        this.um = border.getCore().getUserManager();
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
            this.um.getExactUser(event.getPlayer()).sendMessage("border", "&cYou've reached the border!");
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
        return (spawnPos.squaredDistance(new BlockVector2(to.getX(), to.getZ())) <= this.config.radius * this.config.radius);
    }

    // TODO prevent chunk generation behind the border
}
