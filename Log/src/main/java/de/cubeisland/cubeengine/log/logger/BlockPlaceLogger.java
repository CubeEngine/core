package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockPlaceConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.PLAYER;

public class BlockPlaceLogger extends    BlockLogger<BlockPlaceConfig>
{
    public BlockPlaceLogger(Log module) {
        super(module, BlockPlaceConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        World world = event.getBlock().getWorld();
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            this.log(PLAYER, world, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState(), null);
        }
        this.log(PLAYER,world, event.getPlayer(), event.getBlockReplacedState(), event.getBlockPlaced().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event)
    {
        World world = event.getBlockClicked().getWorld();
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
        this.log(PLAYER, world, event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()).getState(), newState);
    }

    private void log(BlockChangeCause cause,  World world, Player player,BlockState oldState, BlockState newState)
    {
        BlockPlaceConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if (!config.noLogging.contains(newState.getType()))
            {
                this.logBlockChange(cause,world, player, oldState, newState);
            }
        }
    }


}
