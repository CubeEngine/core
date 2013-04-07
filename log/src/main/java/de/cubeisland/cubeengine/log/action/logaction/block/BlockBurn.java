package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;
import static org.bukkit.Material.AIR;

/**
 * Blocks burning
 * <p>Events: {@link BlockBurnEvent}</p>
 * <p>External Actions:
 * {@link BlockActionType#logAttachedBlocks BlockBreak and HangingBreak} when attached Blocks will fall
 * {@link BlockActionType#logFallingBlocks BlockFall} when relative Blocks will fall
 */
public class BlockBurn extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "block-burn";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            BlockState blockState = event.getBlock().getState();
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
            this.logBlockChange(blockState.getLocation(),null,BlockData.of(blockState), AIR, null);
        }
        this.logAttachedBlocks(event.getBlock().getState(), null);
        this.logFallingBlocks(event.getBlock().getState(), null);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &awent up into flames%s!",
                            time,
                            logEntry.getOldBlock(),
                            loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BLOCK_BURN_enable;
    }
}
