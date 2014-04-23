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
package de.cubeisland.engine.log.action.block.player;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.BlockFall;
import de.cubeisland.engine.log.action.block.BlockPreFallEvent;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerBlockBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerContainerBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerJukeboxBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerNoteBlockBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerSignBreak;

import static de.cubeisland.engine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.engine.log.action.block.ListenerBlock.logAttachedBlocks;
import static de.cubeisland.engine.log.action.block.ListenerBlock.logFallingBlocks;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.DOWN;
import static org.bukkit.block.BlockFace.UP;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockBreakEvent}
 * {@link BlockPlaceEvent}
 * {@link SignChangeEvent}
 * {@link BlockPhysicsEvent}
 * <p>Actions:
 * {@link de.cubeisland.engine.log.action.block.player.destroy.PlayerBlockBreak}
 * {@link PlayerNoteBlockBreak}
 * {@link PlayerSignBreak}
 * {@link PlayerJukeboxBreak}
 * {@link PlayerContainerBreak}
 * {@link PlayerBlockPlace}
 * {@link de.cubeisland.engine.log.action.block.player.SignChange}
 */
public class ListenerPlayerBlock extends LogListener
{
    public ListenerPlayerBlock(Log module)
    {
        super(module, PlayerBlockBreak.class, PlayerBlockPlace.class, PlayerNoteBlockBreak.class, PlayerSignBreak.class,
              PlayerJukeboxBreak.class, PlayerContainerBreak.class, PlayerBlockPlace.class, SignChange.class);
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

        if (blockState.getType() == OBSIDIAN) // portal?
        {
            // TODO better & complete
            Block block = blockState.getBlock();
            for (BlockFace face : BLOCK_FACES)
            {
                if (block.getRelative(face).getType() == PORTAL)
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
        logAttachedBlocks(this, module.getCore().getEventManager(), event.getBlock(), action);
        logFallingBlocks(this, module.getCore().getEventManager(), event.getBlock(), action);
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
            if (this.isActive(BlockFall.class, blockPlaced.getWorld()) && blockPlaced.getRelative(DOWN).getType() == AIR
                && (blockPlaced.getType().hasGravity() || blockPlaced.getType() == DRAGON_EGG))
            {
                this.module.getCore().getEventManager().fireEvent(new BlockPreFallEvent(blockPlaced.getLocation(),
                                                                                        action));
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
        SignChange action = this.newAction(SignChange.class, event.getBlock().getWorld());
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
}
