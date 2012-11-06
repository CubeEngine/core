package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.logger.SubLogConfig;
import java.util.ArrayList;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import static de.cubeisland.cubeengine.log.logger.blockchange.BlockLogger.BlockChangeCause.PLAYER;

public class BlockPlaceLogger extends BlockLogger<BlockPlaceLogger.BlockPlaceConfig>
{
    public BlockPlaceLogger()
    {
        this.config = new BlockPlaceConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            this.logBlockChange(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState(), null);
        }
        this.logBlockChange(PLAYER, event.getPlayer(), event.getBlockReplacedState(), event.getBlockPlaced().getState());
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
        this.logBlockChange(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()).getState(), newState);
        // TODO Care when replacing if it is Bucket ID!!!
    }

    public static class BlockPlaceConfig extends SubLogConfig
    {
        public BlockPlaceConfig()
        {
            this.enabled = true;
        }
        @Option(value = "no-logging", genericType = BlockData.class)
        public ArrayList<BlockData> noLogging = new ArrayList<BlockData>();

        @Override
        public String getName()
        {
            return "block-place";
        }
    }
}
