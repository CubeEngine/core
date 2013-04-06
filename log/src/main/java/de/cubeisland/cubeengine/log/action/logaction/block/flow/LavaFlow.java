package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockForm;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;
import static org.bukkit.Material.*;

/**
 * Lava-flow
 * <p>Events: {@link FlowActionType}</p>
 * <p>External Actions:
 * {@link LavaBreak}, {@link BlockForm}
 */
public class LavaFlow extends BlockActionType
{
    public LavaFlow(Log module)
    {
        super(module, BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "lava-flow";
    }


    public void logLavaFlow(BlockFromToEvent event, BlockState toBlock, BlockState newToBlock, BlockState fromBlock)
    {

        if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
        {
            if (event.getFace().equals(BlockFace.DOWN))
            {
                this.logBlockForm(toBlock, newToBlock, STONE);
            }
            else
            {
                this.logBlockForm(toBlock, newToBlock, COBBLESTONE);
            }
            return;
        }
        if (toBlock.getType().equals(Material.REDSTONE_WIRE) && BlockUtil.isSurroundedByWater(event.getToBlock()))
        {
            this.logBlockForm(toBlock, newToBlock, OBSIDIAN);
            return;
        }
        if (fromBlock.getRawData() <= 4 && BlockUtil.isSurroundedByWater(event.getToBlock()))
        {
            this.logBlockForm(toBlock, newToBlock, COBBLESTONE);
            return;
        }
        newToBlock.setType(Material.LAVA);
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
            if (toBlock.getType().equals(Material.LAVA) || toBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                return; // changing lava-level do not log
            }
            this.logLavaBreak(toBlock,newToBlock);
        }
    }

    private void logLavaBreak(BlockState toBlock, BlockState newToBlock)
    {
        LavaBreak lavaBreak = this.manager.getActionType(LavaBreak.class);
        if (lavaBreak.isActive(toBlock.getWorld()))
        {
            lavaBreak.logBlockChange(null,toBlock,newToBlock,null);
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

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {//TODO attach
        user.sendTranslated("%s&aLava occupied the block%s!",time,loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LAVA_FLOW_enable;
    }
}
