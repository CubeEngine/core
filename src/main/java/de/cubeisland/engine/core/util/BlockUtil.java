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
package de.cubeisland.engine.core.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import static net.minecraft.server.v1_7_R4.Block.REGISTRY;
import static org.bukkit.Material.*;

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

    public static final BlockFace[] CARDINAL_DIRECTIONS = new BlockFace[]
        {
            BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH
        };

    private static net.minecraft.server.v1_7_R4.Block getBlockForId(int id)
    {
        return (net.minecraft.server.v1_7_R4.Block)REGISTRY.a(id);
    }

    /**
     * Searches for blocks that are attached onto given block.
     *
     * @param block
     * @return the attached blocks
     */
    public static Collection<Block> getAttachedBlocks(Block block)
    {
        Collection<Block> blocks = new HashSet<>();
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
            case SAND:
            case GRAVEL:
            return true;
            default: return false;
        }
    }

    public static Collection<Block> getDetachableBlocksOnTop(Block block)
    {
        Collection<Block> blocks = new HashSet<>();
        Block onTop = block.getRelative(BlockFace.UP);
        while (isDetachableFromBelow(onTop.getType()))
        {
            blocks.add(onTop);
            for (Block attachedBlock : getAttachedBlocks(onTop))
            {
                blocks.add(attachedBlock);
                blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
            }
            onTop = onTop.getRelative(BlockFace.UP);
        }
        return blocks;
    }

    public static Collection<Block> getDetachableBlocks(Block block)
    {
        Collection<Block> blocks = new HashSet<>();

        for (Block attachedBlock : getAttachedBlocks(block))
        {
            blocks.add(attachedBlock);
            blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
        }
        blocks.addAll(getDetachableBlocksOnTop(block));
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

    public static boolean isInvertedStep(MaterialData stepData)
    {
        return (stepData.getData() & 0x8) == 0x8;
    }

    /**
     * All fluid blocks
     */
    private static final EnumSet<Material> FLUID_BLOCKS = EnumSet.of(WATER, STATIONARY_WATER, LAVA, STATIONARY_LAVA);

    public static boolean isFluidBlock(Material mat)
    {
        return FLUID_BLOCKS.contains(mat);
    }

    /**
     * Blocks that can get destroyed by fluids
     */
    private static final EnumSet<Material> NON_FLUID_PROOF_BLOCKS =
        EnumSet.of(SAPLING, POWERED_RAIL, DETECTOR_RAIL, WEB, LONG_GRASS, DEAD_BUSH, YELLOW_FLOWER,
                   RED_ROSE, BROWN_MUSHROOM, RED_MUSHROOM, TORCH, FIRE, REDSTONE_WIRE, CROPS, LEVER, REDSTONE_TORCH_OFF,
                   REDSTONE_TORCH_ON, SNOW, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, VINE, WATER_LILY,
                   NETHER_WARTS, COCOA, TRIPWIRE_HOOK, TRIPWIRE, FLOWER_POT, CARROT, POTATO, SKULL,
                   ACTIVATOR_RAIL, REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON);

    public static boolean isNonFluidProofBlock(Material mat)
    {
        return NON_FLUID_PROOF_BLOCKS.contains(mat);
    }

    private static final EnumSet<Material> NON_OBSTRUCTING_SOLID_BLOCKS =
        EnumSet.of(SIGN_POST, WOODEN_DOOR, WALL_SIGN, STONE_PLATE, IRON_DOOR_BLOCK,
                   WOOD_PLATE, GOLD_PLATE, IRON_PLATE);

    public static boolean isNonObstructingSolidBlock(Material material)
    {
        return NON_OBSTRUCTING_SOLID_BLOCKS.contains(material);
    }

    public static boolean isPowerSource(Material type)
    {
        return getBlockForId(type.getId()).isPowerSource();
    }

    /**
     * On BlockPlaceEvent a door will orientate its hinge according to the returned data for the top door-half
     *
     * @param placeLocation the location where the lower door half is placed
     * @param player the player placing the door
     * @return the top-data
     */
    public static byte getTopDoorDataOnPlace(Material doorType, Location placeLocation, Player player)
    {
        byte dir1 = 0;
        byte dir2 = 0;
        switch ((int) Math.floor(((player.getLocation().getYaw() + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3)
        {
            case 0: dir2 = 1; break;
            case 1: dir1 = -1; break;
            case 2: dir2 = -1; break;
            case 3: dir1 = 1; break;
        }
        Material negLocType = placeLocation.clone().add(-dir1, 0, -dir2).getBlock().getType();
        Material negLocUpType = placeLocation.clone().add(-dir1, 1, -dir2).getBlock().getType();
        Material posgLocType = placeLocation.clone().add(dir1, 0, dir2).getBlock().getType();
        Material posLocUpType = placeLocation.clone().add(dir1, 1, dir2).getBlock().getType();
        int hingeBlockSide1 = (isHingeBlock(negLocType) ? 1 : 0) + (isHingeBlock(negLocUpType) ? 1 : 0);
        int hingeBlockSide2 = (isHingeBlock(posgLocType) ? 1 : 0) + (isHingeBlock(posLocUpType) ? 1 : 0);
        boolean foundDoorSide1 = negLocType == doorType || negLocUpType == doorType;
        boolean foundDoorSide2 =  posgLocType == doorType || posLocUpType == doorType;
        return (byte)(8 | (((foundDoorSide1 && !foundDoorSide2) || (hingeBlockSide2 > hingeBlockSide1)) ? 1 : 0));
    }

    private static boolean isHingeBlock(Material material)
    {
        net.minecraft.server.v1_7_R4.Block block = getBlockForId(material.getId());
        // called in ItemDoor.place(...)
        return block.r(); // return (this.material.k()) && (d()) && (!isPowerSource());
    }

    public static Block getHighestBlockAt(Location loc)
    {
        return getHighestBlockAt(loc.getWorld(), loc.getBlockX(), loc.getBlockZ());
    }
    
    @SuppressWarnings("deprecation")
    public static Block getHighestBlockAt(World world, final int x, final int z)
    {
        int y = world.getMaxHeight() - 1;
        
        while (world.getBlockTypeIdAt(x, y, z) == 0 && y > 0)
        {
            --y;
        }
        
        return world.getBlockAt(x, y, z);
    }
}
