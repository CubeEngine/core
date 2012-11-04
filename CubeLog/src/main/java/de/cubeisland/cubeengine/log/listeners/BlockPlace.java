package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import static de.cubeisland.cubeengine.log.LogManager.BlockChangeCause.PLAYER;

public class BlockPlace extends LogListener
{
    public BlockPlace(Log module)
    {
        super(module, new PlaceConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            lm.logChangeBlock(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState(), null);
        }
        lm.logChangeBlock(PLAYER, event.getPlayer(), event.getBlockReplacedState(), event.getBlockPlaced().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event)
    {
        BlockState newState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        Material mat = event.getBucket();
        switch (mat)
        {
            case LAVA_BUCKET:
                mat = Material.STATIONARY_LAVA;
                break;
            case WATER_BUCKET:
                mat = Material.STATIONARY_WATER;
                break;
        }
        newState.setType(mat);
        lm.logChangeBlock(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()).getState(), newState);
        // TODO Care when replacing if it is Bucket ID!!!
    }

    public static class PlaceConfig extends LogSubConfiguration
    {
        public PlaceConfig()
        {
            this.actions.put(LogAction.BLOCKPLACE, true);
            this.enabled = true;
        }

        @Override
        public String getName()
        {
            return "place";
        }
    }
}