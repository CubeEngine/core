package de.cubeisland.cubeengine.core.util;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Attachable;

/**
 * Provides Utils for blocks in Bukkit.
 */
public class BlockUtil
{
    private static final BlockFace[] blockFaces =
                                                {
                                                BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH
                                                };

    /**
     * Searches for blocks that are attached onto given block.
     * 
     * @param block
     * @return the attached blocks
     */
    public static Collection<Block> getAttachedBlocks(Block block)
    {
        Collection<Block> blocks = new ArrayList<Block>();
        for (BlockFace bf : blockFaces)
        {
            if (block.getRelative(bf).getState().getData() instanceof Attachable)
            {
                if (((Attachable)block.getRelative(bf).getState().getData()).getAttachedFace().getOppositeFace().equals(bf))
                {
                    blocks.add(block.getRelative(bf));
                }
            }
        }
        return blocks;
    }
}
