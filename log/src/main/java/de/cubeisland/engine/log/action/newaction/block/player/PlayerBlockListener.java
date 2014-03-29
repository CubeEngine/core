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
package de.cubeisland.engine.log.action.newaction.block.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;

import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerBlockBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerContainerBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerJukeboxBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerNoteBlockBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerSignBreak;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerBlockPlace;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.HangingPreBreakEvent;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy.PlayerHangingBreak;

import static de.cubeisland.engine.core.util.BlockUtil.BLOCK_FACES;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.UP;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockBreakEvent}
 * {@link BlockPlaceEvent}
 * {@link SignChangeEvent}
 * {@link BlockPhysicsEvent}
 * <p>Actions:
 * {@link PlayerBlockBreak}
 * {@link PlayerBlockPlace}
 */
public class PlayerBlockListener extends LogListener
{
    private volatile boolean clearPlanned = false;
    private final Map<Location,PlayerBlockBreak> plannedPyhsics = new ConcurrentHashMap<>();

    public PlayerBlockListener(Log module)
    {
        super(module);
    }

    //Doors / Beds only logged bottom / feet
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getBlock().getType() == AIR)
        {
            return; // breaking air !? -> no logging
        }
        if (!this.isActive(PlayerBlockBreak.class, event.getBlock().getWorld()))
        {
            return;
        }
        BlockState blockState = event.getBlock().getState();
        PlayerBlockBreak action;
        if (blockState instanceof NoteBlock)
        {
            action = this.newAction(PlayerNoteBlockBreak.class);
            ((PlayerNoteBlockBreak)action).setNote(((NoteBlock)blockState).getNote());
        }
        else if (blockState instanceof Sign)
        {
            action = this.newAction(PlayerSignBreak.class);
            ((PlayerSignBreak)action).setLines(((Sign)blockState).getLines());
        }
        else if (blockState instanceof Jukebox && ((Jukebox)blockState).getPlaying() != null)
        {
            action = this.newAction(PlayerJukeboxBreak.class);
            ((PlayerJukeboxBreak)action).setDisc(((Jukebox)blockState).getPlaying());
        }
        else if (blockState instanceof InventoryHolder)
        {
            action = this.newAction(PlayerContainerBreak.class, event.getBlock().getWorld());
            if (action == null)
            {
                action = this.newAction(PlayerBlockBreak.class);
            }
            else
            {
                ((PlayerContainerBreak)action).setContents(((InventoryHolder)blockState).getInventory().getContents());
            }
            // TODO item drops
            // itemDrop.logDropsFromChest((InventoryHolder)blockState,location,event.getPlayer());
        }
        else
        {
            action = this.newAction(PlayerBlockBreak.class);
            blockState = adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
        }
        action.setPlayer(event.getPlayer());
        action.setLocation(event.getBlock().getLocation());
        action.setOldBlock(blockState);
        action.setNewBlock(AIR);
        this.logAction(action);

        if (blockState.getType().equals(OBSIDIAN)) // portal?
        {
            // TODO better & complete
            Block block = blockState.getBlock();
            for (BlockFace face : BLOCK_FACES)
            {
                if (block.getRelative(face).getType().equals(PORTAL))
                {
                    Block portal = block.getRelative(face);
                    PlayerBlockBreak pAction = this.newAction(PlayerBlockBreak.class);
                    pAction.setPlayer(event.getPlayer());
                    pAction.setLocation(portal.getLocation());
                    pAction.setOldBlock(portal.getState());
                    pAction.setNewBlock(AIR);
                    pAction.reference = this.reference(action);
                    this.logAction(pAction);
                    break;
                }
            }
        }
        // TODO attached & falling
        this.logAttachedBlocks(blockState, action);
        this.logFallingBlocks(blockState, event.getPlayer());
    }

    private void logAttachedBlocks(BlockState state, PlayerBlockBreak action)
    {
        if (!state.getType().isSolid() && !(state.getType() == Material.SUGAR_CANE_BLOCK))
        {
            return; // cannot have attached
        }

        for (Block block : BlockUtil.getAttachedBlocks(state.getBlock()))
        {
            this.preplanBlockPhyiscs(block.getLocation(),  action);
        }
        for (Block block : BlockUtil.getDetachableBlocksOnTop(state.getBlock()))
        {
            if (block.getData() < 8
                || !(block.getType().equals(Material.WOODEN_DOOR)
                || block.getType().equals(Material.IRON_DOOR_BLOCK))) // ignore upper door halfs
            {
                this.preplanBlockPhyiscs(block.getLocation(),  action);
            }
        }

        if (this.isActive(PlayerHangingBreak.class, state.getWorld()))
        {
            Location location = state.getLocation();
            Location entityLocation = state.getLocation();
            for (Entity entity : state.getBlock().getChunk().getEntities())
            {
                if (entity instanceof Hanging && location.distanceSquared(entity.getLocation(entityLocation)) < 4)
                {
                    this.module.getCore().getEventManager().fireEvent(new HangingPreBreakEvent(entityLocation, action));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block blockPlaced = event.getBlockPlaced();
        PlayerBlockPlace action = this.newAction(PlayerBlockPlace.class, blockPlaced.getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setPlayer(event.getPlayer());
            action.setOldBlock(event.getBlockReplacedState());
            action.setNewBlock(blockPlaced.getState());
            this.logAction(action);
            if (blockPlaced.getRelative(BlockFace.DOWN).getType() == AIR && (blockPlaced.getType()
                                                                                        .hasGravity() || blockPlaced
                .getType() == DRAGON_EGG))
            {
                // TODO block fall
                /*
                BlockFall blockFall = this.manager.getActionType(BlockFall.class);
                if (blockFall.isActive(location.getWorld()))
                {
                    blockFall.preplanBlockFall(location.clone(), event.getPlayer(), this); // TODO this does not seem to work (check me)
                }
                 */
            }
        }
        Block lily = blockPlaced.getRelative(UP);
        if (blockPlaced.getType() != STATIONARY_WATER && lily.getType() == WATER_LILY)
        {
            PlayerBlockBreak wAction = this.newAction(PlayerBlockBreak.class);
            if (wAction != null)
            {
                wAction.setPlayer(event.getPlayer());
                wAction.setLocation(lily.getLocation());
                wAction.setOldBlock(lily.getState());
                wAction.setNewBlock(AIR);
                wAction.reference = this.reference(action);
                this.logAction(wAction);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event)
    {
        PlayerSignChange action = this.newAction(PlayerSignChange.class, event.getBlock().getWorld());
        if (action != null)
        {
            String[] oldLines = ((Sign)event.getBlock().getState()).getLines();
            boolean isEmpty = true;
            boolean wasEmpty = true;
            for (String line : event.getLines())
            {
                if (!line.isEmpty())
                {
                    isEmpty = false;
                }
            }
            for (String line : oldLines)
            {
                if (!line.isEmpty())
                {
                    wasEmpty = false;
                }
            }
            if (wasEmpty && isEmpty)
            {
                return;
            }
            action.setPlayer(event.getPlayer());
            action.setLocation(event.getBlock().getLocation());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getBlock().getState());
            if (!wasEmpty)
            {
                action.setOldLines(oldLines);
            }
            if (!isEmpty)
            {
                action.setNewLines(event.getLines());
            }
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(final BlockPhysicsEvent event)
    {
        if (!this.isActive(PlayerBlockBreak.class, event.getBlock().getWorld())) return;
        BlockState oldState = event.getBlock().getState();
        Block blockAttachedTo;
        if (oldState.getData() instanceof Attachable)
        {
            Attachable attachable = (Attachable) oldState.getData();
            if (attachable.getAttachedFace() == null) return; // is not attached !?
            blockAttachedTo = event.getBlock().getRelative(attachable.getAttachedFace());
        }
        else // block on bottom missing
        {
            if (!BlockUtil.isDetachableFromBelow(oldState.getType()))
            {
                return;
            }
            blockAttachedTo = event.getBlock().getRelative(BlockFace.DOWN);
        }
        if (blockAttachedTo == null) return;
        if (!blockAttachedTo.getType().isSolid())
        {
            Location loc = oldState.getLocation();
            PlayerBlockBreak cause = this.plannedPyhsics.remove(loc);
            if (cause != null)
            {
                oldState = adjustBlockForDoubleBlocks(oldState);
                PlayerBlockBreak action;
                if (oldState instanceof Sign)
                {
                    action = this.newAction(PlayerSignBreak.class);
                    ((PlayerSignBreak)action).setLines(((Sign)oldState).getLines());
                }
                else
                {
                    action = this.newAction(PlayerBlockBreak.class);
                }
                action.setOldBlock(oldState);
                action.setNewBlock(AIR);
                action.player = cause.player;
                action.reference = this.reference(cause);
                this.logAction(action);
            }
        }
    }

    private void preplanBlockPhyiscs(Location location, PlayerBlockBreak action)
    {
        plannedPyhsics.put(location,action);
        if (!clearPlanned)
        {
            clearPlanned = true;
            this.module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    plannedPyhsics.clear();
                }
            });
        }
    }

}
