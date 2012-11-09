package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.PLAYER;

public class BlockBreakLogger extends BlockLogger<BlockBreakLogger.BlockBreakConfig>
{
    public BlockBreakLogger()
    {
        this.config = new BlockBreakConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        //TODO SAND stuff later...
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            this.logBlockChange(PLAYER, event.getPlayer(), block.getState(), null);
        }
        switch (event.getBlock().getRelative(BlockFace.UP).getType())
        {
            case WOODEN_DOOR:
            case IRON_DOOR:
            case SNOW:
            case SEEDS:
            case LONG_GRASS:
            case SUGAR_CANE_BLOCK:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case NETHER_WARTS:
            case DEAD_BUSH:
            case SAPLING:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_WIRE:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case CACTUS:
                this.logBlockChange(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState(), null);
        }
        this.logBlockChange(PLAYER, event.getPlayer(), event.getBlock().getState(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event)
    {
        if (event.getBlockClicked().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            this.logBlockChange(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(BlockFace.UP).getState(), null);
        }
        this.logBlockChange(PLAYER, event.getPlayer(), event.getBlockClicked().getState(), null);
    }

    public static class BlockBreakConfig extends SubLogConfig
    {
        public BlockBreakConfig()
        {
            this.enabled = true;
        }
        @Option(value = "no-logging", valueType = BlockData.class)
        public ArrayList<BlockData> noLogging = new ArrayList<BlockData>();

        @Override
        public String getName()
        {
            return "block-break";
        }
    }
}
