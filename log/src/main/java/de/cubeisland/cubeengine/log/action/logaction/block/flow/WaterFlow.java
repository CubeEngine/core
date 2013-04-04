package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockForm;

import static de.cubeisland.cubeengine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.cubeengine.core.util.BlockUtil.DIRECTIONS;
import static de.cubeisland.cubeengine.log.storage.ActionType.BLOCK_FORM;
import static de.cubeisland.cubeengine.log.storage.ActionType.WATER_BREAK;
import static de.cubeisland.cubeengine.log.storage.ActionType.WATER_FLOW;
import static org.bukkit.Material.*;

public class WaterFlow extends BlockActionType
{
    public WaterFlow(Log module)
    {
        super(module, 0x36, "water-flow");
    }

    public void logWaterFlow(BlockFromToEvent event, BlockState toBlock, BlockState newToBlock, BlockState fromBlock)
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
                this.logBlockForm(toBlock,newToBlock,WATER);
            }// else only changing water-level do not log
            return;
        }
        if (newToBlock.getType().equals(Material.LAVA) || newToBlock.getType().equals(Material.STATIONARY_LAVA) && newToBlock.getRawData() <= 2)
        {
            this.logBlockForm(toBlock,newToBlock,COBBLESTONE);
            return;
        }
        for (final BlockFace face : BLOCK_FACES)
        {
            if (face.equals(BlockFace.UP))continue;
            final Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType().equals(Material.LAVA) && nearBlock.getState().getRawData() <=4 || nearBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                BlockState oldNearBlock = nearBlock.getState();
                BlockState newNearBlock = nearBlock.getState();
                this.logBlockForm(oldNearBlock,newNearBlock,nearBlock.getData() == 0 ? OBSIDIAN : COBBLESTONE);
            }
        }
        newToBlock.setType(Material.WATER);
        newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
        if (toBlock.getType().equals(AIR))
        {
            if (this.isActive(toBlock.getWorld()))
            {
                this.logBlockChange(null,toBlock,newToBlock,null);
            }
        }
        else
        {
            this.logWaterBreak(toBlock,newToBlock);
        }
    }

    private void logWaterBreak(BlockState toBlock, BlockState newToBlock)
    {
        WaterBreak waterBreak = this.manager.getActionType(WaterBreak.class);
        if (waterBreak.isActive(toBlock.getWorld()))
        {
            waterBreak.logBlockChange(null,toBlock,newToBlock,null);
        }
    }

    private void logBlockForm(BlockState toBlock, BlockState newToBlock, Material newType)
    {
        BlockForm blockForm = this.manager.getActionType(BlockForm.class);
        if (blockForm.isActive(toBlock.getWorld()))
        {
            newToBlock.setType(newType);
            newToBlock.setRawData((byte)0);
            blockForm.logBlockChange(null,toBlock,newToBlock,null);
        }
    }
}
