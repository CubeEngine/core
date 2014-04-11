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
package de.cubeisland.engine.log.action.block;

import java.lang.reflect.Constructor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.Redoable;
import de.cubeisland.engine.log.action.Rollbackable;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerContainerBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerJukeboxBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerNoteBlockBreak;
import de.cubeisland.engine.log.action.block.player.destroy.PlayerSignBreak;
import de.cubeisland.engine.reflect.Section;

import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.UP;

public abstract class ActionBlock extends BaseAction implements Rollbackable, Redoable
{
    public BlockSection oldBlock;
    public BlockSection newBlock;

    protected ActionBlock(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    public void setOldBlock(BlockState state)
    {
        this.oldBlock = new BlockSection(state);
    }

    public void setNewBlock(BlockState state)
    {
        this.newBlock = new BlockSection(state);
    }

    public void setOldBlock(Material mat)
    {
        this.oldBlock = new BlockSection(mat);
    }

    public void setNewBlock(Material mat)
    {
        this.newBlock = new BlockSection(mat);
    }

    public static class BlockSection implements Section
    {
        public Material material;
        public Byte data;

        public BlockSection()
        {
        }

        public BlockSection(BlockState state)
        {
            this(state.getType());
            this.data = state.getRawData();
        }

        public BlockSection(Material material)
        {
            this.material = material;
            this.data = 0;
        }

        /**
         * Returns true if this BlockSection is one of given materials
         */
        public boolean is(Material... materials)
        {
            for (Material mat : materials)
            {
                if (this.material == mat)
                {
                    return true;
                }
            }
            return false;
        }

        public String name()
        {
            return this.material.name();
        }

        public <T extends MaterialData> T as(Class<T> clazz)
        {
            try
            {
                Constructor<T> constructor = clazz.getConstructor(Material.class);
                T instance = constructor.newInstance(material);
                instance.setData(this.data);
                return instance;
            }
            catch (ReflectiveOperationException e)
            {
                return null;
            }
        }
    }

    @Override
    public boolean rollback(LogAttachment attachment, boolean force, boolean preview)
    {
        return this.setBlock(this.oldBlock, this.coord.toLocation(), attachment, force, preview, true);
    }


    @Override
    public boolean redo(LogAttachment attachment, boolean force, boolean preview)
    {
        return this.setBlock(this.newBlock, this.coord.toLocation(), attachment, force, preview, false);
    }

    protected boolean setBlock(BlockSection blockData, Location loc, LogAttachment attachment, boolean force, boolean preview, boolean rollback)
    {
        Block block = loc.getBlock();
        BlockState state = loc.getBlock().getState();
        state.setType(blockData.material);
        if (blockData.material == IRON_DOOR_BLOCK || blockData.material == WOODEN_DOOR)
        {
            byte data = (byte)(blockData.data & ~8); // TODO correct?
            state.setRawData(data);
        }
        else
        {
            state.setRawData(blockData.data);
        }
        if (!force && (state.getData() instanceof Attachable // TODO correct?
            || BlockUtil.isDetachableFromBelow(blockData.material)))
        {
            return false;
        }
        if (state.getData() instanceof Bed)
        {
            Bed bed = (Bed)state.getData();
            Block headBed = block.getRelative(bed.getFacing());
            BlockState headState = headBed.getState();
            headState.setType(AIR);
            if (preview)
            {
                attachment.addToPreview(headState);
            }
            else
            {
                headState.update(true, false);
            }
        }
        else if (state.getType() == WOOD_DOOR || state.getType() == IRON_DOOR_BLOCK)
        {
            Block topDoor = block.getRelative(UP);
            if (topDoor.getType() == state.getType())
            {
                BlockState topState = topDoor.getState();
                topState.setType(AIR);
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
            state.update(true, false);
        }
        if (rollback)
        {
            if (this instanceof SignBreak || this instanceof PlayerSignBreak)
            {
                String[] lines = this instanceof SignBreak ? ((SignBreak)this).oldLines : ((PlayerSignBreak)this).oldLines;
                if (preview)
                {
                    attachment.addToPreview(state.getLocation(), lines);
                }
                else
                {
                    Sign sign = (Sign)state.getBlock().getState();
                    int i = 0;
                    for (String line : lines)
                    {
                        sign.setLine(i++, line);
                    }
                    sign.update();
                }
            }
            else if (blockData.is(BED_BLOCK))
            {
                Bed bed = (Bed)state.getData();
                BlockState headBed = block.getRelative(bed.getFacing()).getState();
                headBed.setType(BED_BLOCK);
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
            }
            else if (blockData.is(WOOD_DOOR, IRON_DOOR_BLOCK))
            {
                byte data = (byte)(((blockData.data & 8) == 8) ? 9 : 8);
                BlockState topDoor = block.getRelative(UP).getState();
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
            }
            else if (!preview)
            {
                if (this instanceof PlayerNoteBlockBreak)
                {
                    NoteBlock noteblock = (NoteBlock)state.getBlock().getState();
                    noteblock.setNote(((PlayerNoteBlockBreak)this).note);
                    noteblock.update();
                }
                else if (this instanceof PlayerJukeboxBreak)
                {
                    Jukebox jukebox = (Jukebox)state.getBlock().getState();
                    jukebox.setPlaying(((PlayerJukeboxBreak)this).disc);
                    jukebox.update();
                }
                else if (this instanceof PlayerContainerBreak)
                {
                    InventoryHolder inventoryHolder = (InventoryHolder)state.getBlock().getState();
                    inventoryHolder.getInventory().setContents(((PlayerContainerBreak)this).contents);
                    ((BlockState)inventoryHolder).update();
                }
            }
        }
        return true;
    }

    @Override
    public boolean isBlockBound()
    {
        return true;
    }

    @Override
    public boolean isStackable()
    {
        return false;
    }
}
