package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.material.Bed;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BlockBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.player.HangingBreak;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public abstract class BlockActionType extends LogActionType
{
    public BlockActionType(Log module, String name, boolean canRollback, Category... types)
    {
        super(module, name, canRollback,types);
    }

    public BlockActionType(Log module, String name, Category... types)
    {
        super(module, name, false,types);
    }

    public void logBlockChange(Location location, Entity causer, BlockData oldState, BlockData newState, String additional)
    {
        this.logBlockChange(location, causer,
                            oldState.material.name(), oldState.data.longValue(),
                            newState.material.name(), newState.data, additional);
    }

    public void logBlockChange(Location location, Entity causer, BlockData oldState, Material newState, String additional)
    {
        this.logBlockChange(location,causer,oldState.material.name(),oldState.data.longValue(),newState.name(),(byte)0,additional);
    }

    public void logBlockChange(Location location, Entity causer, Material oldState, BlockData newState, String additional)
    {
        this.logBlockChange(location,causer,oldState.name(),0L,newState.material.name(),newState.data,additional);
    }

    private void logBlockChange(Location location, Entity causer, String block, Long data, String newBlock, Byte newData, String additional)
    {
        this.queueLog(location,causer,block,data.longValue(),newBlock,newData,additional);
    }

    public void logBlockChange(Location location, Entity causer, Material oldBlock, Material newBlock, String additional)
    {
        this.queueLog(location,causer,oldBlock.name(),0L,newBlock.name(),(byte)0,additional);
    }

    /**
     * oldBlock is not allowed to be null!
     */
    public void logBlockChange(Entity causer, BlockState oldBlock, BlockState newBlock, String additional)
    {
        this.logBlockChange(oldBlock.getLocation(),causer,BlockData.of(oldBlock),BlockData.of(newBlock),additional);
    }

    public static class BlockData
    {
        public Material material;
        public Byte data;

        private BlockData(BlockState blockState)
        {
            material = blockState.getType();
            data = blockState.getRawData();
        }

        public BlockData(Material mat, byte data)
        {
            this.material = mat;
            this.data = data;
        }

        public static BlockData of(BlockState state)
        {
            return new BlockData(state);
        }

        public static BlockData of(Material mat, byte data)
        {
            return new BlockData(mat,data);
        }
    }

    /**
     * Only the bottom half of doors and the feet of a bed is logged!
     *
     * @param blockState
     * @return
     */
    public final BlockState adjustBlockForDoubleBlocks(BlockState blockState)
    {
        if (blockState.getType().equals(Material.WOOD_DOOR) || blockState.getType().equals(Material.IRON_DOOR_BLOCK))
        {
            if (blockState.getRawData() == 8 || blockState.getRawData() == 9)
            {
                return blockState.getBlock().getRelative(BlockFace.DOWN).getState();
            }
        }
        else if (blockState instanceof Bed)
        {
            if (((Bed)blockState).isHeadOfBed())
            {
                return blockState.getBlock().getRelative(((Bed)blockState).getFacing().getOppositeFace()).getState();
            }
        }
        return blockState;
    }

    public void logAttachedBlocks(BlockState blockState, Entity player)
    {
        if (!blockState.getType().isSolid() && !blockState.getType().equals(Material.SUGAR_CANE_BLOCK))
        {
            return; // cannot have attached
        }
        BlockBreak blockBreak = this.manager.getActionType(BlockBreak.class);
        if (blockBreak.isActive(blockState.getWorld()))
        {
            for (Block block : BlockUtil.getAttachedBlocks(blockState.getBlock()))
            {
                blockBreak.preplanBlockPhyiscs(block.getLocation(), player, this);
            }
            for (Block block : BlockUtil.getDetachableBlocksOnTop(blockState.getBlock()))
            {
                blockBreak.preplanBlockPhyiscs(block.getLocation(), player, this);
            }
        }
        HangingBreak hangingBreak = this.manager.getActionType(HangingBreak.class);
        if (hangingBreak.isActive(blockState.getWorld()))
        {
            Location location = blockState.getLocation();
            Location entityLocation = blockState.getLocation();
            for (Entity entity : blockState.getBlock().getChunk().getEntities())
            {
                if (entity instanceof Hanging && location.distanceSquared(entity.getLocation(entityLocation)) < 4)
                {
                    hangingBreak.preplanHangingBreak(entity.getLocation(),player);
                }
            }
        }
    }

    public void logFallingBlocks(BlockState blockState, Entity player)
    {
        // Falling Blocks
        BlockFall blockFall = this.manager.getActionType(BlockFall.class);
        if (blockFall.isActive(blockState.getWorld()))
        {
            Block onTop = blockState.getBlock().getRelative(BlockFace.UP);
            if (onTop.getType().hasGravity()||onTop.getType().equals(Material.DRAGON_EGG))
            {
                blockFall.preplanBlockFall(onTop.getLocation(),player,this);
            }
        }
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.block.equals(other.block)
            && logEntry.newBlock.equals(other.newBlock)
            && logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.additional == other.additional; // additional is null
    }
}
