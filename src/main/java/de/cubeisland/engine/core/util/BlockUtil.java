/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static org.spongepowered.api.block.BlockTypes.*;
import static org.spongepowered.api.util.Direction.*;

/**
 * Provides Utils for blocks in Bukkit.
 */
public class BlockUtil
{
    public static final org.spongepowered.api.util.Direction[] BLOCK_FACES = {
        DOWN, UP, EAST, NORTH, WEST, SOUTH
    };

    public static final org.spongepowered.api.util.Direction[] DIRECTIONS = {
        DOWN, NORTH, WEST, EAST, SOUTH
    };

    public static final org.spongepowered.api.util.Direction[] CARDINAL_DIRECTIONS = {
        NORTH, WEST, EAST, SOUTH
    };

    private static net.minecraft.server.v1_8_R2.Block getBlockForId(int id)
    {
        return (net.minecraft.server.v1_8_R2.Block)REGISTRY.a(id);
    }

    /**
     * Searches for blocks that ar
     * e attached onto given block.
     *
     * @return the attached blocks
     */
    public static Collection<Location> getAttachedBlocks(Location block)
    {
        Collection<Location> blocks = new HashSet<>();
        for (org.spongepowered.api.util.Direction bf : BLOCK_FACES)
        {
            if (block.getRelative(bf).getState().getData() instanceof Attachable)
            {
                if (((Attachable)block.getRelative(bf).getState().getData()).getAttachedFace().getOppositeFace().equals(
                    bf))
                {
                    blocks.add(block.getRelative(bf));
                }
            }
        }
        return blocks;
    }

    public static boolean isDetachableFromBelow(BlockType mat)
    {
        switch (mat)
        {
            case BROWN_MUSHROOM:
            case CARROTS:
            case DEADBUSH:
            case DETECTOR_RAIL:
            case POTATOES:
            case WHEAT:
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
            default:
                return false;
        }
    }

    public static Collection<Location> getDetachableBlocksOnTop(Location block)
    {
        Collection<Location> blocks = new HashSet<>();
        Location onTop = block.getRelative(UP);
        while (isDetachableFromBelow(onTop.getType()))
        {
            blocks.add(onTop);
            for (Location attachedBlock : getAttachedBlocks(onTop))
            {
                blocks.add(attachedBlock);
                blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
            }
            onTop = onTop.getRelative(UP);
        }
        return blocks;
    }

    public static Collection<Location> getDetachableBlocks(Location block)
    {
        Collection<Location> blocks = new HashSet<>();

        for (Location attachedBlock : getAttachedBlocks(block))
        {
            blocks.add(attachedBlock);
            blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
        }
        blocks.addAll(getDetachableBlocksOnTop(block));
        return blocks;
    }

    public static boolean isSurroundedByWater(Location block)
    {
        for (final Direction face : DIRECTIONS)
        {
            BlockType type = block.getRelative(face).getType();
            if (type == WATER || type == FLOWING_WATER)
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
    private static final Set<BlockType> FLUID_BLOCKS = new HashSet<>(Arrays.asList(WATER, FLOWING_WATER, LAVA,
                                                                                   FLOWING_LAVA));

    public static boolean isFluidBlock(BlockType mat)
    {
        return FLUID_BLOCKS.contains(mat);
    }

    /**
     * Blocks that can get destroyed by fluids
     */
    private static final Set<BlockType> NON_FLUID_PROOF_BLOCKS = new HashSet<>(Arrays.asList(SAPLING, GOLDEN_RAIL,
                                                                                             DETECTOR_RAIL, WEB,
                                                                                             TALLGRASS, DEADBUSH,
                                                                                             YELLOW_FLOWER, RED_FLOWER,
                                                                                             BROWN_MUSHROOM,
                                                                                             RED_MUSHROOM, TORCH, FIRE,
                                                                                             REDSTONE_WIRE, WHEAT,
                                                                                             LEVER,
                                                                                             UNLIT_REDSTONE_TORCH,
                                                                                             REDSTONE_TORCH, SNOW,
                                                                                             UNPOWERED_REPEATER,
                                                                                             POWERED_REPEATER,
                                                                                             PUMPKIN_STEM, MELON_STEM,
                                                                                             VINE, WATERLILY,
                                                                                             NETHER_WART, COCOA,
                                                                                             TRIPWIRE_HOOK, TRIPWIRE,
                                                                                             FLOWER_POT, CARROTS,
                                                                                             POTATOES, SKULL,
                                                                                             ACTIVATOR_RAIL,
                                                                                             POWERED_COMPARATOR,
                                                                                             UNPOWERED_COMPARATOR));

    public static boolean isNonFluidProofBlock(BlockType mat)
    {
        return NON_FLUID_PROOF_BLOCKS.contains(mat);
    }

    private static final Set<BlockType> NON_OBSTRUCTING_SOLID_BLOCKS = new HashSet<>(Arrays.asList(STANDING_SIGN,
                                                                                                   WALL_SIGN,
                                                                                                   WOODEN_DOOR,
                                                                                                   IRON_DOOR,
                                                                                                   ACACIA_DOOR,
                                                                                                   BIRCH_DOOR,
                                                                                                   DARK_OAK_DOOR,
                                                                                                   JUNGLE_DOOR,
                                                                                                   SPRUCE_DOOR,
                                                                                                   STONE_PRESSURE_PLATE,
                                                                                                   WOODEN_PRESSURE_PLATE,
                                                                                                   LIGHT_WEIGHTED_PRESSURE_PLATE,
                                                                                                   HEAVY_WEIGHTED_PRESSURE_PLATE));

    public static boolean isNonObstructingSolidBlock(BlockType material)
    {
        return NON_OBSTRUCTING_SOLID_BLOCKS.contains(material);
    }

    public static boolean isPowerSource(BlockType type)
    {
        return getBlockForId(type.getId()).isPowerSource();
    }

    /**
     * On BlockPlaceEvent a door will orientate its hinge according to the returned data for the top door-half
     *
     * @param placeLocation the location where the lower door half is placed
     * @param player        the player placing the door
     *
     * @return the top-data
     */
    public static byte getTopDoorDataOnPlace(BlockType doorType, Location placeLocation, Player player)
    {
        byte dir1 = 0;
        byte dir2 = 0;
        switch ((int)Math.floor(((player.getLocation().getYaw() + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3)
        {
            case 0:
                dir2 = 1;
                break;
            case 1:
                dir1 = -1;
                break;
            case 2:
                dir2 = -1;
                break;
            case 3:
                dir1 = 1;
                break;
        }
        BlockType negLocType = placeLocation.clone().add(-dir1, 0, -dir2).getBlock().getType();
        BlockType negLocUpType = placeLocation.clone().add(-dir1, 1, -dir2).getBlock().getType();
        BlockType posgLocType = placeLocation.clone().add(dir1, 0, dir2).getBlock().getType();
        BlockType posLocUpType = placeLocation.clone().add(dir1, 1, dir2).getBlock().getType();
        int hingeBlockSide1 = (isHingeBlock(negLocType) ? 1 : 0) + (isHingeBlock(negLocUpType) ? 1 : 0);
        int hingeBlockSide2 = (isHingeBlock(posgLocType) ? 1 : 0) + (isHingeBlock(posLocUpType) ? 1 : 0);
        boolean foundDoorSide1 = negLocType == doorType || negLocUpType == doorType;
        boolean foundDoorSide2 = posgLocType == doorType || posLocUpType == doorType;
        return (byte)(8 | (((foundDoorSide1 && !foundDoorSide2) || (hingeBlockSide2 > hingeBlockSide1)) ? 1 : 0));
    }

    private static boolean isHingeBlock(BlockType material)
    {
        net.minecraft.server.v1_8_R2.Block block = getBlockForId(material.getId());
        // called in ItemDoor.place(...)
        return block.isOccluding(); // return (this.material.k()) && (d()) && (!isPowerSource());
    }

    public static Location getHighestBlockAt(Location loc)
    {
        return getHighestBlockAt((World)loc.getExtent(), loc.getBlockX(), loc.getBlockZ());
    }

    @SuppressWarnings("deprecation")
    public static Location getHighestBlockAt(World world, final int x, final int z)
    {
        int y = world.getBuildHeight() - 1;

        while (world.getBlockType(x, y, z) == AIR && y > 0)
        {
            --y;
        }

        return world.getFullBlock(x, y, z);
    }
}
