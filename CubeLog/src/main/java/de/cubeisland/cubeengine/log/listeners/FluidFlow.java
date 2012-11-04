package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import static de.cubeisland.cubeengine.log.LogManager.BlockChangeCause.*;

public class FluidFlow extends LogListener
{
    public FluidFlow(Log module)
    {
        super(module, new FluidConfig());
    }
    //TODO do this better
    private static final Set<Integer> nonFluidProofBlocks = new HashSet<Integer>(Arrays.asList(7, 8, 9, 10, 27, 28, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 69, 70, 75, 76, 78, 93, 94, 104, 105, 106));
    private static final BlockFace[] DIRECTIONS = new BlockFace[]
    {
        BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
    };

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        Material mat = event.getBlock().getType();
        BlockState fromBlock = event.getBlock().getState();
        BlockState toBlock = event.getToBlock().getState();
        BlockState newToBlock = event.getToBlock().getState();
        final boolean canFlow = toBlock.getType().equals(Material.AIR) || nonFluidProofBlocks.contains(toBlock.getTypeId());
        if (!canFlow)
        {
            return;
        }
        if (mat.equals(Material.LAVA) || mat.equals(Material.STATIONARY_LAVA))
        {
            //TODO when removing source of falling lava "AIR" is falling down no more lava but blocks get formed
            if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
            {
                if (event.getFace() == BlockFace.DOWN)
                {
                    newToBlock.setType(Material.STONE);
                    newToBlock.setRawData((byte)0);
                }
                else
                {
                    newToBlock.setType(Material.COBBLESTONE);
                    newToBlock.setRawData((byte)0);
                }
            }
            else if (this.isSurroundedByWater(event.getToBlock()) && toBlock.getType().equals(Material.REDSTONE_WIRE))
            {
                newToBlock.setType(Material.OBSIDIAN);
                newToBlock.setRawData((byte)0);
            }
            else if (this.isSurroundedByWater(event.getToBlock()) && fromBlock.getRawData() <= 2)
            {

                newToBlock.setType(Material.COBBLESTONE);
                newToBlock.setRawData((byte)0);
            }
            else if (toBlock.getType().equals(Material.AIR))
            {
                newToBlock.setType(Material.LAVA);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            else
            {
                if (toBlock.getType().equals(Material.LAVA) || toBlock.getType().equals(Material.STATIONARY_LAVA))
                {
                    return; // changing lava-level do not log
                }
                newToBlock.setType(Material.LAVA);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            lm.logChangeBlock(LAVA, null, toBlock, newToBlock);
        }
        else if (mat.equals(Material.WATER) || mat.equals(Material.STATIONARY_WATER))
        {
            if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
            {
                int sources = 0;
                for (BlockFace face : DIRECTIONS)
                {
                    Block nearBlock = event.getToBlock().getRelative(face);
                    if (nearBlock.getType().equals(Material.STATIONARY_WATER) && nearBlock.getData() == 0)
                    {
                        sources++;
                    }
                }
                if (sources >= 2) // created new source block
                {
                    newToBlock.setType(Material.STATIONARY_WATER);
                    newToBlock.setRawData((byte)0);
                    lm.logChangeBlock(WATER, null, toBlock, newToBlock);
                }
                return; // changing water-level do not log
            }
            if (newToBlock.getType().equals(Material.LAVA) || newToBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                newToBlock.setType(Material.COBBLESTONE);
                newToBlock.setRawData((byte)0);
            }
            else
            {
                for (final BlockFace face : DIRECTIONS)
                {
                    final Block nearBlock = event.getToBlock().getRelative(face);
                    if (nearBlock.getType().equals(Material.LAVA) || nearBlock.getType().equals(Material.STATIONARY_LAVA))
                    {
                        BlockState oldNearBlock = nearBlock.getState();
                        BlockState newNearBlock = nearBlock.getState();
                        newNearBlock.setTypeId(nearBlock.getData() == 0 ? 49 : 4);
                        newNearBlock.setRawData((byte)0);
                        lm.logChangeBlock(WATER, null, oldNearBlock, newNearBlock);
                    }
                }
                newToBlock.setType(Material.WATER);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            lm.logChangeBlock(WATER, null, toBlock, newToBlock);
        }
    }

    private boolean isSurroundedByWater(Block block)
    {
        for (final BlockFace face : DIRECTIONS)
        {
            final int type = block.getRelative(face).getTypeId();
            if (type == 8 || type == 9)
            {
                return true;
            }
        }
        return false;
    }

    public static class FluidConfig extends LogSubConfiguration
    {
        public FluidConfig()
        {
            this.actions.put(LogAction.LAVAFLOW, false);
            this.actions.put(LogAction.WATERFLOW, false);
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "fluid";
        }
    }
}
