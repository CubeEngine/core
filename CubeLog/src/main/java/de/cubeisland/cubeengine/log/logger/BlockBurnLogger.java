package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.FIRE;

public class BlockBurnLogger extends
    BlockLogger<BlockBurnLogger.BlockBurnConfig>
{
    public BlockBurnLogger()
    {
        this.config = new BlockBurnConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            this.logBlockChange(FIRE, null, block.getState(), null);
        }
        switch (event.getBlock().getRelative(BlockFace.UP).getType())
        {
            case WOODEN_DOOR:
            case IRON_DOOR:
            case SNOW:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_WIRE:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
                this.logBlockChange(FIRE, null, event.getBlock().getRelative(BlockFace.UP).getState(), null);
        }
        this.logBlockChange(FIRE, null, event.getBlock().getState(), null);
    }

    public static class BlockBurnConfig extends SubLogConfig
    {
        @Override
        public String getName()
        {
            return "block-burn";
        }
    }
}
