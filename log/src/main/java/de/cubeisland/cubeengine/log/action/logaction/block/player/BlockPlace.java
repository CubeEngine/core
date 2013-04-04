package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.bukkit.Material.AIR;

public class BlockPlace extends BlockActionType
{
    public BlockPlace(Log module)
    {
        super(module, 0x20, "block-place");
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
                json.put("break-cause", this.actionTypeID);
                blockBreak.logBlockChange(state.getLocation(),event.getPlayer(),oldData,AIR,json.toString());
            }
        }
    }
}
