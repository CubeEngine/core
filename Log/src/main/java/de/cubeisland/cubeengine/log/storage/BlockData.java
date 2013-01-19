package de.cubeisland.cubeengine.log.storage;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;

public class BlockData
{
    public Material mat;
    public byte data;

    public BlockData(Material mat, byte data)
    {
        this.mat = mat;
        this.data = data;
    }

    public static BlockData get(BlockState state)
    {
        if (state == null)
        {
            return null;
        }
        if (state.getType() == Material.NOTE_BLOCK)
        {
            return new BlockData(state.getType(), ((NoteBlock) state).getRawNote());
        }
        return new BlockData(state.getType(), state.getRawData());
    }

    public static BlockData get(BlockState state, byte customData)
    {
        if (state == null)
        {
            return null;
        }
        return new BlockData(state.getType(), customData);
    }

    public BlockData(Block block)
    {
        this.mat = block.getType();
        this.data = block.getData();
    }

    public BlockState applyTo(BlockState state)
    {
        state.setType(mat);
        state.setRawData(data);
        return state;
    }
}
