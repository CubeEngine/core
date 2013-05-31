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
package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.material.Attachable;
import org.bukkit.material.Bed;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BlockBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.player.HangingBreak;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class BlockActionType extends LogActionType
{
    public void logBlockChange(Location location, Entity causer, BlockData oldState, BlockData newState, String additional)
    {
        this.logBlockChange(location, causer,
                            oldState.material.name(), oldState.data.longValue(),
                            newState.material.name(), newState.data, additional);
    }

    public void logBlockChange(Location location, Entity causer, BlockData oldState, Material newState, String additional)
    {
        this.logBlockChange(location,causer,oldState.material.name(),oldState.data.longValue(),newState.name(),(byte)0,additional);
    }

    public void logBlockChange(Location location, Entity causer, Material oldState, BlockData newState, String additional)
    {
        this.logBlockChange(location,causer,oldState.name(),0L,newState.material.name(),newState.data,additional);
    }

    private void logBlockChange(Location location, Entity causer, String block, Long data, String newBlock, Byte newData, String additional)
    {
        if (this.isBreakingBlockIgnored(location.getWorld(),block) || this.isPlacingBlockIgnored(location.getWorld(),newBlock))
        {
            return;
        }
        this.queueLog(location,causer,block,data,newBlock,newData,additional);
    }

    private boolean isBreakingBlockIgnored(World world, String block)
    {
        if (block == null || block.equals("AIR"))
        {
            return false;
        }
        Material mat = Material.getMaterial(block);
        if (this.lm.getConfig(world).breakNoLogging.contains(mat))
        {
            return true;
        }
        return false;
    }
    private boolean isPlacingBlockIgnored(World world, String block)
    {
        if (block == null || block.equals("AIR"))
        {
            return false;
        }
        Material mat = Material.getMaterial(block);
        return this.lm.getConfig(world).placeNoLogging.contains(mat);
    }

    public void logBlockChange(Location location, Entity causer, Material oldBlock, Material newBlock, String additional)
    {
        this.logBlockChange(location, causer, oldBlock.name(), 0L, newBlock.name(), (byte)0, additional);
    }

    public void logBlockChange(Entity causer, BlockState oldBlock, BlockState newBlock, String additional)
    {
        this.logBlockChange(oldBlock.getLocation(),causer,BlockData.of(oldBlock),BlockData.of(newBlock),additional);
    }

    public static class BlockData
    {
        public Material material;
        public Byte data;

        private BlockData(BlockState blockState)
        {
            material = blockState.getType();
            data = blockState.getRawData();
        }

        public BlockData(Material mat, byte data)
        {
            this.material = mat;
            this.data = data;
        }

        public static BlockData of(BlockState state)
        {
            return new BlockData(state);
        }

        public static BlockData of(Material mat, byte data)
        {
            return new BlockData(mat,data);
        }
    }

    /**
     * Only the bottom half of doors and the feet of a bed is logged!
     *
     * @param blockState
     * @return
     */
    public final BlockState adjustBlockForDoubleBlocks(BlockState blockState)
    {
        if (blockState.getType().equals(Material.WOODEN_DOOR) || blockState.getType().equals(Material.IRON_DOOR_BLOCK))
        {
            if (blockState.getRawData() == 8 || blockState.getRawData() == 9)
            {
                return blockState.getBlock().getRelative(BlockFace.DOWN).getState();
            }
        }
        else if (blockState.getData() instanceof Bed)
        {
            Bed bed = (Bed)blockState.getData();
            if (bed.isHeadOfBed())
            {
                return blockState.getBlock().getRelative(bed.getFacing().getOppositeFace()).getState();
            }
        }
        return blockState;
    }

    public void logAttachedBlocks(BlockState blockState, Entity player)
    {
        if (!blockState.getType().isSolid() && !blockState.getType().equals(Material.SUGAR_CANE_BLOCK))
        {
            return; // cannot have attached
        }
        BlockBreak blockBreak = this.manager.getActionType(BlockBreak.class);
        if (blockBreak.isActive(blockState.getWorld()))
        {
            for (Block block : BlockUtil.getAttachedBlocks(blockState.getBlock()))
            {
                blockBreak.preplanBlockPhyiscs(block.getLocation(), player, this);
            }
            for (Block block : BlockUtil.getDetachableBlocksOnTop(blockState.getBlock()))
            {
                if (block.getData() < 8
                    || !(block.getType().equals(Material.WOODEN_DOOR)
                      || block.getType().equals(Material.IRON_DOOR_BLOCK))) // ignore upper door halfs
                {
                    blockBreak.preplanBlockPhyiscs(block.getLocation(), player, this);
                }
            }
        }
        HangingBreak hangingBreak = this.manager.getActionType(HangingBreak.class);
        if (hangingBreak.isActive(blockState.getWorld()))
        {
            Location location = blockState.getLocation();
            Location entityLocation = blockState.getLocation();
            for (Entity entity : blockState.getBlock().getChunk().getEntities())
            {
                if (entity instanceof Hanging && location.distanceSquared(entity.getLocation(entityLocation)) < 4)
                {
                    hangingBreak.preplanHangingBreak(entity.getLocation(),player);
                }
            }
        }
    }

    public void logFallingBlocks(BlockState blockState, Entity player)
    {
        // Falling Blocks
        BlockFall blockFall = this.manager.getActionType(BlockFall.class);
        if (blockFall.isActive(blockState.getWorld()))
        {
            Block onTop = blockState.getBlock().getRelative(BlockFace.UP);
            if (onTop.getType().hasGravity()||onTop.getType().equals(Material.DRAGON_EGG))
            {
                blockFall.preplanBlockFall(onTop.getLocation(),player,this);
            }
        }
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if ((logEntry.newBlock == other.newBlock || logEntry.newBlock.equals(other.newBlock))
            && logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.additional == other.additional) // additional
        {
            if (logEntry.block.equals(other.block))
            {
                return nearTimeFrame(logEntry,other);
            }
            else
            {
                if (logEntry.block.equals("LAVA") || logEntry.block.equals("STATIONARY_LAVA"))
                {
                    if (other.block.equals("LAVA") || other.block.equals("STATIONARY_LAVA"))
                    {
                        return nearTimeFrame(logEntry,other);
                    }
                }
                else if (logEntry.block.equals("WATER") || logEntry.block.equals("STATIONARY_WATER"))
                {
                    if (other.block.equals("WATER") || other.block.equals("STATIONARY_WATER"))
                    {
                        return nearTimeFrame(logEntry,other);
                    }
                }
            }
        }
        return false;
    }

    private boolean nearTimeFrame(LogEntry logEntry, LogEntry other)
    {
        return logEntry.causer <= 0 ||
            Math.abs(TimeUnit.MILLISECONDS.toSeconds(logEntry.timestamp.getTime() - other.timestamp.getTime())) < 5;
    }

    @Override
    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        de.cubeisland.cubeengine.log.storage.BlockData oldBlock = logEntry.getOldBlock();
        Block block = logEntry.getLocation().getBlock();
        BlockState state = block.getState();
        state.setType(oldBlock.material);
        state.setRawData(oldBlock.data);
        if (!force && (state.getData() instanceof Attachable || BlockUtil.isDetachableFromBelow(oldBlock.material)))
        {
            return false;
        }
        switch (block.getType())
        {
        case BED_BLOCK:
            // TODO remove head too
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
        switch (oldBlock.material)
        {
        case SIGN_POST:
        case WALL_SIGN:
            Sign sign = (Sign)block.getState();
            if (logEntry.getAdditional() != null)
            {
                ArrayNode oldSign = (ArrayNode)logEntry.getAdditional().get("oldSign");
                if (oldSign == null)
                {
                    oldSign = (ArrayNode)logEntry.getAdditional().get("sign");
                }
                sign.setLine(0,oldSign.get(0).textValue());
                sign.setLine(1,oldSign.get(1).textValue());
                sign.setLine(2,oldSign.get(2).textValue());
                sign.setLine(3,oldSign.get(3).textValue());
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
            noteBlock.setRawNote(oldBlock.data);
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
            BlockState topDoor = block.getRelative(BlockFace.UP).getState();
            topDoor.setType(state.getType());
            topDoor.setRawData((byte)8);
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
}
