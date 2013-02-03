package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.util.math.BlockVector2;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BorderListener implements Listener
{
    private BorderConfig config;

    public BorderListener(BorderConfig config)
    {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Chunk to = event.getTo().getChunk();

        if (!event.getFrom().getChunk().equals(to))
        {
            Chunk spawnChunk = to.getWorld().getSpawnLocation().getChunk();
            BlockVector2 spawnPos = new BlockVector2(spawnChunk.getX(), spawnChunk.getZ());
            double distanceSquared = spawnPos.squaredDistance(new BlockVector2(to.getX(), to.getZ()));
            if (distanceSquared > this.config.radius)
            {
                event.setCancelled(true);
            }
        }
    }

    // TODO prevent players from generating new chunks
}
