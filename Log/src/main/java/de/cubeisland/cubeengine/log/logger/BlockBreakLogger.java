package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.SubLogConfig;
import java.util.Collection;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.PLAYER;

public class BlockBreakLogger extends
    BlockLogger<BlockBreakLogger.BlockBreakConfig>
{
    public BlockBreakLogger()
    {
        this.config = new BlockBreakConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            this.log(PLAYER, event.getPlayer(), block.getState());
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
                this.log(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState());
        }
        this.log(PLAYER, event.getPlayer(), event.getBlock().getState());

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event)
    {
        if (event.getBlockClicked().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            this.log(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(BlockFace.UP).getState());
        }
        this.log(PLAYER, event.getPlayer(), event.getBlockClicked().getState());
    }

    private void log(BlockChangeCause cause, Player player, BlockState oldState)
    {
        if (!this.config.noLogging.contains(oldState.getType()))
        {
            this.logBlockChange(cause, player, oldState, null);
        }
    }

    public static class BlockBreakConfig extends SubLogConfig
    {
        public BlockBreakConfig()
        {
            this.enabled = true;
        }

        @Option("no-logging")
        public Collection<Material> noLogging = new LinkedList<Material>();

        @Override
        public String getName()
        {
            return "block-break";
        }
    }
}
