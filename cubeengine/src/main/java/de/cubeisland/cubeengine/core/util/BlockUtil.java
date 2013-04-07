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
package de.cubeisland.cubeengine.core.util;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Attachable;

/**
 * Provides Utils for blocks in Bukkit.
 */
public class BlockUtil
{
    public static final BlockFace[] BLOCK_FACES =
    {
        BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH
    };

    public static final BlockFace[] DIRECTIONS = new BlockFace[]
    {
            BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
    };

    /**
     * Searches for blocks that are attached onto given block.
     *
     * @param block
     * @return the attached blocks
     */
    public static Collection<Block> getAttachedBlocks(Block block)
    {
        Collection<Block> blocks = new HashSet<Block>();
        for (BlockFace bf : BLOCK_FACES)
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

    public static boolean isDetachableFromBelow(Material mat)
    {
        switch(mat)
        {
            case BROWN_MUSHROOM:
            case CARROT:
            case DEAD_BUSH:
            case DETECTOR_RAIL:
            case POTATO:
            case CROPS:
            case DIODE:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case FLOWER_POT:
            case IRON_DOOR:
            case IRON_DOOR_BLOCK:
            case LEVER:
            case LONG_GRASS:
            case MELON_STEM:
            case NETHER_WARTS:
            case PORTAL:
            case POWERED_RAIL:
            case ACTIVATOR_RAIL:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case GOLD_PLATE:
            case IRON_PLATE:
            case PUMPKIN_STEM:
            case RAILS:
            case RED_MUSHROOM:
            case RED_ROSE:
            case REDSTONE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case REDSTONE_WIRE:
            case SAPLING:
            case SIGN:
            case SIGN_POST:
            case SKULL:
            case SNOW:
            case STONE_PLATE:
            case TORCH:
            case TRIPWIRE:
            case WATER_LILY:
            case WHEAT:
            case WOOD_DOOR:
            case WOOD_PLATE:
            case WOODEN_DOOR:
            case YELLOW_FLOWER:
            case SUGAR_CANE_BLOCK:
            case CACTUS:
            return true;
            default: return false;
        }
    }

    public static Collection<Block> getDetachableBlocksOnTop(Block block)
    {
        Collection<Block> blocks = new HashSet<Block>();
        Block onTop = block.getRelative(BlockFace.UP);
        if (onTop.getType().equals(Material.SUGAR_CANE_BLOCK) || onTop.getType().equals(Material.CACTUS))
        {
            blocks.add(onTop);
            onTop = onTop.getRelative(BlockFace.UP);
            while (onTop.getType().equals(Material.SUGAR_CANE_BLOCK) || onTop.getType().equals(Material.CACTUS))
            {
                blocks.add(onTop);
                onTop = onTop.getRelative(BlockFace.UP);
            }
        }
        else if (isDetachableFromBelow(onTop.getType()))
        {
            blocks.add(onTop);
        }
        return blocks;
    }

    public static boolean isSurroundedByWater(Block block)
    {
        for (final BlockFace face : DIRECTIONS)
        {
            final int type = block.getRelative(face).getTypeId();
            if (type == 8 || type == 9)
            {
                return true;
            }
        }
        return false;
    }
}
