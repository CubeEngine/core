/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.logaction.block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.log.action.ActionType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.DRAGON_EGG;

/**
 * Blocks fading
 * <p>Events: {@link BlockPhysicsEvent} {@link BlockActionType#logFallingBlocks preplanned external} </p>
 */
public class BlockFall extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, ENVIRONEMENT, PLAYER));
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
                    json.put("fall-cause",cause.getRight().getModel().getId().longValue());
                    this.logBlockChange(loc, cause.getLeft(), BlockData.of(state), AIR, json.toString());

                    Block onTop = state.getBlock().getRelative(BlockFace.UP);
                    if (onTop.getType().hasGravity() || onTop.getType().equals(DRAGON_EGG))
                    {
                        this.preplanBlockFall(onTop.getLocation(),cause.getLeft(),cause.getRight());
                    }
                }
            }
        }
    }

    private final Map<Location,Pair<Entity,BlockActionType>> plannedFall = new ConcurrentHashMap<>();
    public void preplanBlockFall(final Location location, Entity player, BlockActionType reason)
    {
        plannedFall.put(location, new Pair<>(player, reason));
        BlockFall.this.logModule.getCore().getTaskManager().runTaskDelayed(logModule, new Runnable()
        {
            @Override
            public void run()
            {
                BlockFall.this.plannedFall.remove(location);
            }
        }, 3);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getCauserUser() == null)
        {
            ActionType type = this.manager.getActionType(logEntry.getAdditional().get("cause").asInt());
            user.sendTranslated("%s&6%s&a did fall to a lower place %s&a because of &6%s",
                                time,logEntry.getOldBlock(), loc,type.getName());
        }
        else
        {
            user.sendTranslated("%s&2%s &acaused &6%s&a to fall to a lower place%s",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getOldBlock(),loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.BLOCK_FALL_enable;
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.getNewblock().equals(other.getNewblock())
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getCauser().equals(other.getCauser())
            && logEntry.getAdditional().equals(other.getAdditional()) // additional
            && logEntry.getBlock() == other.getBlock();
    }
}
