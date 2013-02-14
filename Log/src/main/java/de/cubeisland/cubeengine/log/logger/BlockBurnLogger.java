package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockBurnConfig;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FIRE;

public class BlockBurnLogger extends BlockLogger<BlockBurnConfig>
{
    public BlockBurnLogger(Log module)
    {
        super(module, BlockBurnConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        World world = event.getBlock().getWorld();
        BlockBurnConfig config = this.configs.get(world);
        if (config.enabled)
        {
            for (Block block : BlockUtil.getAttachedBlocks(event.getBlock())) // attached blockss
            {
                this.logBlockChange(FIRE, world, null, block.getState(), null);
            }
            switch (event.getBlock().getRelative(BlockFace.UP).getType())
            // blocks on top that get destroyed
            {
                case WOODEN_DOOR:
                case IRON_DOOR:
                case SNOW:
                case STONE_PLATE:
                case WOOD_PLATE:
                case REDSTONE_WIRE:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    this.logBlockChange(FIRE, world, null, event.getBlock().getRelative(BlockFace.UP).getState(), null);
            }
            this.logBlockChange(FIRE, world, null, event.getBlock().getState(), null);
        }
    }
}
