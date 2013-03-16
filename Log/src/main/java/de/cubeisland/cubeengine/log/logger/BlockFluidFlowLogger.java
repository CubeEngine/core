package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.logger.config.BlockFluidFlowConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.EnumSet;
import java.util.Set;

import static de.cubeisland.cubeengine.core.bukkit.BlockUtil.isFluidBlock;
import static de.cubeisland.cubeengine.core.bukkit.BlockUtil.isNonFluidProofBlock;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.LAVA;
import static de.cubeisland.cubeengine.log.logger.BlockLogger.BlockChangeCause.WATER;

public class BlockFluidFlowLogger extends BlockLogger<BlockFluidFlowConfig>
{
    public BlockFluidFlowLogger(Log module)
    {
        super(module, BlockFluidFlowConfig.class);
    }

    //TODO do this better
    private static final BlockFace[] DIRECTIONS = new BlockFace[]
            {
                    BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
            };

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        World world = event.getBlock().getWorld();
        Material mat = event.getBlock().getType();
        BlockState fromBlock = event.getBlock().getState();
        BlockState toBlock = event.getToBlock().getState();
        BlockState newToBlock = event.getToBlock().getState();
        final boolean canFlow = toBlock.getType().equals(Material.AIR) || isNonFluidProofBlock(toBlock.getType());
        if (!canFlow)
        {
            return;
        }
        if (mat.equals(Material.LAVA) || mat.equals(Material.STATIONARY_LAVA))
        {
            //TODO when removing source of falling lava "AIR" is falling down no more lava but blocks get formed //any idea if possible how to log this?
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
            this.log(LAVA, world, toBlock, newToBlock);
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
                    this.log(WATER, world, toBlock, newToBlock);
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
                        this.log(WATER, world, oldNearBlock, newNearBlock);
                    }
                }
                newToBlock.setType(Material.WATER);
                newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
            }
            this.log(WATER, world, toBlock, newToBlock);
        }
    }

    private static Set<Material> lava = EnumSet.of(Material.LAVA, Material.STATIONARY_LAVA);
    private static Set<Material> water = EnumSet.of(Material.WATER, Material.STATIONARY_WATER);

    public void log(BlockChangeCause cause, World world, BlockState oldState, BlockState newState)
    {
        BlockFluidFlowConfig config = this.configs.get(world);
        if (config.enabled)
        {
            if ((water.contains(oldState.getType()) && (water.contains(newState.getType()) || newState.getTypeId() == 0) && !config.logWaterFlow)
                || (lava.contains(oldState.getType()) && (lava.contains(newState.getType()) || newState.getTypeId() == 0) && !config.logLavaFlow))
            {
                return;
            }
            else if (!isFluidBlock(oldState.getType()) && isFluidBlock(newState.getType()))
            {

                if ((lava.contains(newState.getType()) && !config.logLavaDestruct)
                    || (water.contains(newState.getType()) && !config.logWaterDestruct))
                {
                    return;
                }
            }
            else if (!isFluidBlock(newState.getType()) && newState.getTypeId() != 0) //newBlock is not fluid or air
            {
                if (isFluidBlock(oldState.getType()))
                {
                    if (!config.logLavaWaterCreation)
                    {
                        return;
                    }
                }
                else
                {
                    if (oldState.getType().equals(Material.REDSTONE_WIRE)
                        && newState.getType().equals(Material.OBSIDIAN)
                        && !config.logRedsObsiCreation)
                    {
                        return;
                    }
                }
            }
            this.logBlockChange(cause, world, null, oldState, newState);
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

}
