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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;
import org.bukkit.material.Bed;

import com.fasterxml.jackson.databind.node.ArrayNode;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.LogActionType;
import de.cubeisland.engine.log.storage.ImmutableBlockData;
import de.cubeisland.engine.log.storage.LogEntry;

public abstract class BlockActionType extends LogActionType
{
    @Override
    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        ImmutableBlockData oldBlock = logEntry.getOldBlock();
        Block block = logEntry.getLocation().getBlock();
        BlockState state = block.getState();
        state.setType(oldBlock.material);
        return this.setBlock(oldBlock, state, block, attachment, logEntry, preview, force, true);
    }

    private boolean setBlock(ImmutableBlockData blockData, BlockState state, Block block, LogAttachment attachment, LogEntry logEntry, boolean preview, boolean force, boolean rollback)
    {
        if (blockData.material.equals(Material.IRON_DOOR_BLOCK) || blockData.material.equals(Material.WOODEN_DOOR))
        {
            byte data = (byte)(blockData.data & ~8);
            state.setRawData(data);
        }
        else
        {
            state.setRawData(blockData.data);
        }
        if (!force && (state.getData() instanceof Attachable || BlockUtil.isDetachableFromBelow(blockData.material)))
        {
            return false;
        }
        switch (block.getType())
        {
        case BED_BLOCK:
            Bed bed = (Bed)block.getState().getData();
            Block headBed = block.getRelative(bed.getFacing());
            BlockState headState = headBed.getState();
            headState.setType(Material.AIR);
            if (preview)
            {
                attachment.addToPreview(headState);
            }
            else
            {
                headState.update(true, false);
            }
            break;
        case WOODEN_DOOR:
        case IRON_DOOR_BLOCK:
            Block topDoor = block.getRelative(BlockFace.UP);
            if (topDoor.getType().equals(block.getType()))
            {
                BlockState topState = topDoor.getState();
                topState.setType(Material.AIR);
                if (preview)
                {
                    attachment.addToPreview(topState);
                }
                else
                {
                    topState.update(true, false);
                }
            }
        }
        if (preview)
        {
            attachment.addToPreview(state);
        }
        else
        {
            state.update(true,false);
        }
        switch (blockData.material)
        {
        case SIGN_POST:
        case WALL_SIGN:
            Sign sign = (Sign)block.getState(); // TODO ClassCastException here WHY?
            if (logEntry.getAdditional() != null)
            {
                ArrayNode signText;
                if (rollback)
                {
                    signText = (ArrayNode)logEntry.getAdditional().get("oldSign");
                    if (signText == null)
                    {
                        signText = (ArrayNode)logEntry.getAdditional().get("sign"); // This is for old database
                    }
                }
                else
                {
                    signText = (ArrayNode)logEntry.getAdditional().get("sign");
                }
                sign.setLine(0, signText.get(0).textValue());
                sign.setLine(1,signText.get(1).textValue());
                sign.setLine(2, signText.get(2).textValue());
                sign.setLine(3,signText.get(3).textValue());
                if (preview)
                {
                    attachment.addToPreview(sign);
                }
                else
                {
                    sign.update();
                }
            }
            break;
        case NOTE_BLOCK:
            NoteBlock noteBlock = (NoteBlock)block.getState();
            noteBlock.setRawNote((byte)(blockData.data - (rollback ? 0 : 1)));
            if (preview)
            {
                attachment.addToPreview(noteBlock);
            }
            else
            {
                noteBlock.update();
            }
            break;
        case JUKEBOX:
            String playing = logEntry.getAdditional().get("playing").textValue();
            Material mat = Material.getMaterial(playing);
            Jukebox jukebox = (Jukebox)block.getState();
            jukebox.setPlaying(mat);
            if (preview)
            {
                attachment.addToPreview(jukebox);
            }
            else
            {
                jukebox.update();
            }
            break;
        case BED_BLOCK:
            Bed bed = (Bed)state.getData();
            BlockState headBed = block.getRelative(bed.getFacing()).getState();
            headBed.setType(Material.BED_BLOCK);
            Bed bedhead = (Bed)headBed.getData();
            bedhead.setHeadOfBed(true);
            bedhead.setFacingDirection(bed.getFacing());
            if (preview)
            {
                attachment.addToPreview(headBed);
            }
            else
            {
                headBed.update(true);
            }
            break;
        case IRON_DOOR_BLOCK:
        case WOODEN_DOOR:
            byte data = (byte)(((blockData.data & 8) == 8) ? 9 : 8);
            BlockState topDoor = block.getRelative(BlockFace.UP).getState();
            topDoor.setType(state.getType());
            topDoor.setRawData(data);
            if (preview)
            {
                attachment.addToPreview(topDoor);
            }
            else
            {
                topDoor.update(true);
            }
            break;
        // TODO inventoryHolders
        }
        return true;
    }

    @Override
    public boolean redo(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        ImmutableBlockData newBlock = logEntry.getNewBlock();
        Block block = logEntry.getLocation().getBlock();
        BlockState state = block.getState();
        state.setType(newBlock.material);
        return this.setBlock(newBlock, state, block, attachment, logEntry, preview, force, false);
    }
}
