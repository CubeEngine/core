package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENVIRONEMENT;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;
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
    public BlockBurn(Log module)
    {
        super(module, "block-burn", BLOCK, ENVIRONEMENT);
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
}
