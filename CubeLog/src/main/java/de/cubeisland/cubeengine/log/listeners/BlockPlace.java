package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogManager.BlockChangeCause;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

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
            lm.logBreakBlock(BlockChangeCause.PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState());;
        }
        lm.logPlaceBlock(event.getPlayer(), event.getBlockPlaced().getState(), event.getBlockReplacedState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event)
    {
        BlockState newState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        newState.setType(event.getBucket());
        lm.logPlaceBlock(event.getPlayer(), newState, event.getBlockClicked().getRelative(event.getBlockFace()).getState());
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