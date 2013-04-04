package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.storage.ActionType;

import static de.cubeisland.cubeengine.log.storage.ActionType.BLOCK_FALL;
import static de.cubeisland.cubeengine.log.storage.ActionType.HANGING_BREAK;

public class BlockActionType extends LogActionType
{
    public BlockActionType(Log module, int id, String name)
    {
        super(module, id, name);
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

    public static class BlockData
    {
        public Material material;
        public Byte data;

        public BlockData(BlockState blockState)
        {
            material = blockState.getType();
            data = blockState.getRawData();
        }
    }

    /**
     * Only the bottom half of doors and the feet of a bed is logged!
     *
     * @param blockState
     * @return
     */
    protected final BlockState adjustBlockForDoubleBlocks(BlockState blockState)
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

    protected void logAttachedBlocks(BlockState blockState, Entity player)
    {
        if (!blockState.getType().isSolid())
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
    }

    protected void logRelatedBlocks(BlockState blockState, Entity player)
    {
        // Falling Blocks
        Block onTop = blockState.getBlock().getRelative(BlockFace.UP);
        BlockFall blockFall = this.manager.getActionType(BlockFall.class);
        if (blockFall.isActive(blockState.getWorld()))
        {
            while (onTop.getType().equals(Material.SAND)||onTop.getType().equals(Material.GRAVEL)||onTop.getType().equals(Material.ANVIL))
            {
                blockFall.preplanBlockFall(blockState.getLocation(),player,this);
            }
        }
        HangingBreak hangingBreak = this.manager.getActionType(HangingBreak.class);
        if (hangingBreak.isActive(blockState.getWorld()))
        {
            hangingBreak.preplanHangingBreak(blockState.getLocation(),player);
        }
    }
}
