package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static de.cubeisland.cubeengine.log.LogManager.BlockChangeCause.PLAYER;

public class BlockBreak extends LogListener
{
    public BlockBreak(Log module)
    {
        super(module, new BreakConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        //TODO SAND stuff later...
        for (Block block : BlockUtil.getAttachedBlocks(event.getBlock()))
        {
            lm.logBreakBlock(PLAYER, event.getPlayer(), block.getState());
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
                lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlock().getRelative(BlockFace.UP).getState());
        }

        lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event)
    {
        if (event.getBlockClicked().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY))
        {
            lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlockClicked().getRelative(BlockFace.UP).getState());
        }
        lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlockClicked().getState());
    }

    public static class BreakConfig extends LogSubConfiguration
    {
        public BreakConfig()
        {
            this.actions.put(LogAction.PLAYER_BLOCKBREAK, true);
            this.enabled = true;
        }
        
        @Override
        public String getName()
        {
            return "break";
        }
    }
}