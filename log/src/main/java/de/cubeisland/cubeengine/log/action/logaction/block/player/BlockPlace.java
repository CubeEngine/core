package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;
import static org.bukkit.Material.AIR;

/**
 * Blocks placed by a player.
 * <p>Events: {@link BlockPlaceEvent}</p>
 * <p>External Actions: {@link BlockBreak} when breaking waterlily by replacing the water below
 */
public class BlockPlace extends BlockActionType
{
    public BlockPlace(Log module)
    {
        super(module, "block-place", BLOCK, PLAYER);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Location location = event.getBlockPlaced().getLocation();
        if (this.isActive(location.getWorld()))
        {
            BlockData oldData = BlockData.of(event.getBlockReplacedState());
            BlockData newData = BlockData.of(event.getBlockPlaced().getState());
            this.logBlockChange(location,event.getPlayer(),oldData,newData,null);
        }
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY)
            && !event.getBlockPlaced().getType().equals(Material.STATIONARY_WATER))
        {
            BlockBreak blockBreak = this.manager.getActionType(BlockBreak.class);
            if (blockBreak.isActive(location.getWorld()))
            {
                BlockState state = event.getBlock().getRelative(BlockFace.UP).getState();
                BlockData oldData = BlockData.of(state);
                ObjectNode json = this.om.createObjectNode();
                json.put("break-cause", this.getID());
                blockBreak.logBlockChange(state.getLocation(),event.getPlayer(),oldData,AIR,json.toString());
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&2%s &aplaced &6%dx %s&a%s!",
                                time,
                                logEntry.getCauserUser().getDisplayName(),
                                amount,
                                logEntry.getNewBlock(),
                                loc);
        }
        else // single
        {
            if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated("%s&2%s &aplaced &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),
                                    loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &areplaced &6%s&a with &6%s&a%s&a!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getOldBlock(),
                                    logEntry.getNewBlock(),
                                    loc);
            }
        }
    }
}
