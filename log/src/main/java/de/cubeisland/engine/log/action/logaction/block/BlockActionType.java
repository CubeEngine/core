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

import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.LogActionType;
import de.cubeisland.engine.log.action.logaction.block.player.BlockBreak;
import de.cubeisland.engine.log.action.logaction.block.player.HangingBreak;
import de.cubeisland.engine.log.storage.ImmutableBlockData;
import de.cubeisland.engine.log.storage.LogEntry;

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
        return this.lm.getConfig(world).block.breakNoLogging.contains(mat);
    }
    private boolean isPlacingBlockIgnored(World world, String block)
    {
        if (block == null || block.equals("AIR"))
        {
            return false;
        }
        Material mat = Material.getMaterial(block);
        return this.lm.getConfig(world).block.placeNoLogging.contains(mat);
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
                if (blockState.getRawData() == 9)
                {
                    blockState = blockState.getBlock().getRelative(BlockFace.DOWN).getState();
                    blockState.setRawData((byte)(blockState.getRawData()+8));
                    return blockState;
                }
                return blockState.getBlock().getRelative(BlockFace.DOWN).getState();
            }
            else
            {
                if (blockState.getBlock().getRelative(BlockFace.UP).getState().getRawData() == 9)
                {
                    blockState.setRawData((byte)(blockState.getRawData()+8));
                }
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
                    hangingBreak.preplanHangingBreak(entity.getLocation(), player, this);
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
        if (!super.isSimilar(logEntry, other)) return false;
        if ((logEntry.getNewblock() == other.getNewblock() || logEntry.getNewblock().equals(other.getNewblock()))
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getCauser().equals(other.getCauser())
            && logEntry.getAdditional() == other.getAdditional()) // additional
        {
            if (logEntry.getBlock().equals(other.getBlock()))
            {
                return nearTimeFrame(logEntry,other);
            }
            else
            {
                if (logEntry.getBlock().equals("LAVA") || logEntry.getBlock().equals("STATIONARY_LAVA"))
                {
                    if (other.getBlock().equals("LAVA") || other.getBlock().equals("STATIONARY_LAVA"))
                    {
                        return nearTimeFrame(logEntry,other);
                    }
                }
                else if (logEntry.getBlock().equals("WATER") || logEntry.getBlock().equals("STATIONARY_WATER"))
                {
                    if (other.getBlock().equals("WATER") || other.getBlock().equals("STATIONARY_WATER"))
                    {
                        return nearTimeFrame(logEntry,other);
                    }
                }
            }
        }
        return false;
    }

    protected boolean nearTimeFrame(LogEntry logEntry, LogEntry other)
    {
        return logEntry.getCauser() <= 0 ||
            Math.abs(TimeUnit.MILLISECONDS.toSeconds(logEntry.getTimestamp().getTime() - other.getTimestamp().getTime())) < 5;
    }

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
    public boolean canRedo()
    {
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

    @Override
    public boolean canRollback()
    {
        return true;
    }

    @Override
    public boolean isStackable()
    {
        return false;
    }

    @Override
    public boolean isBlockBound()
    {
        return true;
    }
}
