package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.log.Log;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.bukkit.Material.AIR;

public class BlockFall extends BlockActionType
{
    public BlockFall(Log module)
    {
        super(module, 0x41, "block-fall");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        if (!this.isActive(event.getBlock().getWorld())) return;
        BlockState state = event.getBlock().getState();
        if (state.getType().equals(Material.SAND)||state.getType().equals(Material.GRAVEL)||state.getType().equals(Material.ANVIL))
        { // falling blocks
            if (event.getBlock().getRelative(BlockFace.DOWN).getType().equals(AIR))
            {
                Location loc = state.getLocation();
                Pair<Entity,BlockActionType> cause = this.plannedFall.remove(loc);
                if (cause != null)
                {
                    ObjectNode json = this.om.createObjectNode();
                    json.put("fall-cause",cause.getRight().actionTypeID);
                    this.logBlockChange(loc, cause.getLeft(), new BlockData(state), AIR, json.toString());
                }
                else {
                    System.out.print("Unplanned BlockPhysicsEvent! (BlockFall)"); //TODO remove
                }
            }
        }
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Pair<Entity,BlockActionType>> plannedFall = new ConcurrentHashMap<Location, Pair<Entity, BlockActionType>>();
    public void preplanBlockFall(Location location, Entity player, BlockActionType reason)
    {
        plannedFall.put(location, new Pair<Entity, BlockActionType>(player, reason));
        if (!clearPlanned)
        {
            clearPlanned = true;
            BlockFall.this.logModule.getCore().getTaskManager().scheduleSyncDelayedTask(logModule, new Runnable() {
                @Override
                public void run() {
                    clearPlanned = false;
                    BlockFall.this.plannedFall.clear();
                }
            });
        }
    }

}
