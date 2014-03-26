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

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerBlockBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerContainerBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerJukeboxBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerNoteBlockBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerSignBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.indirect.PlayerPortalBreak;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.indirect.PlayerWaterLilyBreak;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerBlockPlace;

import static de.cubeisland.engine.core.util.BlockUtil.BLOCK_FACES;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.UP;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link BlockBreakEvent}
 * {@link BlockPlaceEvent}
 * <p>Actions:
 * {@link de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerBlockBreak}
 * {@link de.cubeisland.engine.log.action.newaction.block.player.place.PlayerBlockPlace}
 * <p>Indirect Actions:
 * {@link de.cubeisland.engine.log.action.newaction.block.player.destroy.indirect.PlayerPortalBreak}
 * {@link de.cubeisland.engine.log.action.newaction.block.player.destroy.indirect.PlayerWaterLilyBreak}
 */
public class PlayerBlockListener extends LogListener
{
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
        if (blockState instanceof NoteBlock) // adjust data (which is always 0 to note)
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
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
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
                    PlayerPortalBreak pAction = this.newAction(PlayerPortalBreak.class);
                    pAction.setPlayer(event.getPlayer());
                    pAction.setLocation(portal.getLocation());
                    pAction.setOldBlock(portal.getState());
                    pAction.setNewBlock(AIR);
                    this.logAction(pAction);
                    break;
                }
            }
        }
        // TODO attached & falling
        this.logAttachedBlocks(blockState, event.getPlayer());
        this.logFallingBlocks(blockState, event.getPlayer());
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
            if (blockPlaced.getRelative(BlockFace.DOWN).getType() == AIR
                && (blockPlaced.getType().hasGravity() || blockPlaced.getType() == DRAGON_EGG))
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
            PlayerWaterLilyBreak wAction = this.newAction(PlayerWaterLilyBreak.class);
            if (wAction != null)
            {
                wAction.setPlayer(event.getPlayer());
                wAction.setLocation(lily.getLocation());
                wAction.setOldBlock(lily.getState());
                wAction.setNewBlock(AIR);
                this.logAction(wAction);
            }
        }
    }
}
