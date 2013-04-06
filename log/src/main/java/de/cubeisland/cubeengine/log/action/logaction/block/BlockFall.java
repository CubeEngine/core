package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.DRAGON_EGG;

/**
 * Blocks fading
 * <p>Events: {@link BlockPhysicsEvent} {@link BlockActionType#logFallingBlocks preplanned external} </p>
 */
public class BlockFall extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT, PLAYER);
    }

    @Override
    public String getName()
    {
        return "block-fall";
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
                    json.put("fall-cause",cause.getRight().getID());
                    this.logBlockChange(loc, cause.getLeft(), BlockData.of(state), AIR, json.toString());

                    Block onTop = state.getBlock().getRelative(BlockFace.UP);
                    if (onTop.getType().hasGravity() || onTop.getType().equals(DRAGON_EGG))
                    {
                        this.preplanBlockFall(onTop.getLocation(),cause.getLeft(),cause.getRight());
                    }
                }
                else {
                    System.out.print("Unplanned BlockPhysicsEvent! (BlockFall) "+state.getType().name()); //TODO remove
                }
            }
        }
    }

    private Map<Location,Pair<Entity,BlockActionType>> plannedFall = new ConcurrentHashMap<Location, Pair<Entity, BlockActionType>>();
    public void preplanBlockFall(final Location location, Entity player, BlockActionType reason)
    {
        plannedFall.put(location, new Pair<Entity, BlockActionType>(player, reason));
        BlockFall.this.logModule.getCore().getTaskManager().scheduleSyncDelayedTask(logModule, new Runnable() {
            @Override
            public void run() {
                BlockFall.this.plannedFall.remove(location);
            }
        },3);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getCauserUser() == null)
        {
            ActionType type = this.manager.getActionType(logEntry.getAdditional().get("cause").asInt());
            user.sendTranslated("%s&6%s&a did fall to a lower place %s&a because of &6%s&a!",
                                time,logEntry.getOldBlock(), loc,type.getName());
        }
        else
        {
            user.sendTranslated("%s&2%s &acaused &6%s&a to fall to a lower place%s!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BLOCK_FALL_enable;
    }

}
