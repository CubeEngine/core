/*
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
package org.cubeengine.libcube.util;

import static org.spongepowered.api.block.BlockTypes.WATER;
import static org.spongepowered.api.util.Direction.DOWN;
import static org.spongepowered.api.util.Direction.EAST;
import static org.spongepowered.api.util.Direction.NORTH;
import static org.spongepowered.api.util.Direction.SOUTH;
import static org.spongepowered.api.util.Direction.UP;
import static org.spongepowered.api.util.Direction.WEST;

import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.AttachmentSurface;
import org.spongepowered.api.data.type.DoorHinge;
import org.spongepowered.api.data.type.DoorHinges;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.storage.ChunkLayout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

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

    /**
     * Searches for blocks that are attached onto given block.
     *
     * @return the attached blocks
     */
    public static Collection<ServerLocation> getAttachedBlocks(ServerLocation block)
    {
        Collection<ServerLocation> blocks = new HashSet<>();
        for (org.spongepowered.api.util.Direction bf : BLOCK_FACES)
        {
            final ServerLocation relative = block.relativeTo(bf);
            final Optional<AttachmentSurface> surface = relative.get(Keys.ATTACHMENT_SURFACE);
            if (surface.isPresent()) {
                final Optional<Direction> direction = relative.get(Keys.DIRECTION);
                if (direction.isPresent() && direction.get().isOpposite(bf)) {
                    blocks.add(relative);
                }
            }
        }
        return blocks;
    }

//    public static Set<BlockType> DETACHABLE_FROM_BELOW = new HashSet<>(Arrays.asList(BROWN_MUSHROOM, CARROTS, DEADBUSH,
//                                                                                     DETECTOR_RAIL, POTATOES, WHEAT,
//                                                                                     POWERED_REPEATER,
//                                                                                     UNPOWERED_REPEATER, FLOWER_POT,
//                                                                                     IRON_DOOR, LEVER, TALLGRASS,
//                                                                                     MELON_STEM, NETHER_WART, PORTAL,
//                                                                                     GOLDEN_RAIL, ACTIVATOR_RAIL,
//                                                                                     POWERED_COMPARATOR,
//                                                                                     UNPOWERED_COMPARATOR,
//                                                                                     HEAVY_WEIGHTED_PRESSURE_PLATE,
//                                                                                     LIGHT_WEIGHTED_PRESSURE_PLATE,
//                                                                                     PUMPKIN_STEM, RAIL, RED_MUSHROOM,
//                                                                                     RED_FLOWER, REDSTONE_WIRE,
//                                                                                     REDSTONE_TORCH,
//                                                                                     UNLIT_REDSTONE_TORCH, SAPLING,
//                                                                                     STANDING_SIGN, WALL_SIGN, SKULL,
//                                                                                     SNOW, STONE_PRESSURE_PLATE, TORCH,
//                                                                                     TRIPWIRE, WATERLILY, WOODEN_DOOR,
//                                                                                     WOODEN_PRESSURE_PLATE,
//                                                                                     YELLOW_FLOWER, REEDS, CACTUS, SAND,
//                                                                                     GRAVEL));

    public static boolean isDetachableFromBelow(BlockType mat)
    {
//        mat.get(Keys.IS_ATTACHED)
        // TODO data
        return false;
    }

    public static Collection<ServerLocation> getDetachableBlocksOnTop(ServerLocation block)
    {
        Collection<ServerLocation> blocks = new HashSet<>();
        ServerLocation onTop = block.relativeTo(UP);
        while (isDetachableFromBelow(onTop.getBlockType()))
        {
            blocks.add(onTop);
            for (ServerLocation attachedBlock : getAttachedBlocks(onTop))
            {
                blocks.add(attachedBlock);
                blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
            }
            onTop = onTop.relativeTo(UP);
        }
        return blocks;
    }

    public static Collection<ServerLocation> getDetachableBlocks(ServerLocation block)
    {
        Collection<ServerLocation> blocks = new HashSet<>();

        for (ServerLocation attachedBlock : getAttachedBlocks(block))
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
            BlockType type = block.relativeTo(face).getBlockType();
            if (type == WATER.get())
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isInvertedStep(ServerLocation location)
    {
        final Optional<PortionType> data = location.get(Keys.PORTION_TYPE);
        if (data.isPresent())
        {
            return data.get() == PortionTypes.TOP.get();
        }
        return false;
    }

    public static boolean isFluidBlock(BlockType mat)
    {
        return false;
    }

    /**
     * Blocks that can get destroyed by fluids
     */
//    private static final Set<BlockType> NON_FLUID_PROOF_BLOCKS = new HashSet<>(Arrays.asList(SAPLING, GOLDEN_RAIL,
//                                                                                             DETECTOR_RAIL, WEB,
//                                                                                             TALLGRASS, DEADBUSH,
//                                                                                             YELLOW_FLOWER, RED_FLOWER,
//                                                                                             BROWN_MUSHROOM,
//                                                                                             RED_MUSHROOM, TORCH, FIRE,
//                                                                                             REDSTONE_WIRE, WHEAT,
//                                                                                             LEVER,
//                                                                                             UNLIT_REDSTONE_TORCH,
//                                                                                             REDSTONE_TORCH, SNOW,
//                                                                                             UNPOWERED_REPEATER,
//                                                                                             POWERED_REPEATER,
//                                                                                             PUMPKIN_STEM, MELON_STEM,
//                                                                                             VINE, WATERLILY,
//                                                                                             NETHER_WART, COCOA,
//                                                                                             TRIPWIRE_HOOK, TRIPWIRE,
//                                                                                             FLOWER_POT, CARROTS,
//                                                                                             POTATOES, SKULL,
//                                                                                             ACTIVATOR_RAIL,
//                                                                                             POWERED_COMPARATOR,
//                                                                                             UNPOWERED_COMPARATOR));

    public static boolean isNonFluidProofBlock(BlockType mat)
    {
        return false; // TODO
//        return NON_FLUID_PROOF_BLOCKS.contains(mat);
    }

    public static boolean isNonObstructingSolidBlock(BlockType material)
    {
        return material.getDefaultState().get(Keys.IS_PASSABLE).get();
    }

    public static Chunk getChunk(ServerLocation block, Game game)
    {
        ChunkLayout cl = game.getServer().getChunkLayout();
        return block.getWorld().getChunk(cl.toChunk(block.getBlockPosition()).get());
    }

    public static Direction getOtherDoorDirection(Direction direction, DoorHinge hinge)
    {
        if (direction == NORTH)
        {
            direction = EAST;
        }
        else if (direction == EAST)
        {
            direction = SOUTH;
        }
        else if (direction == SOUTH)
        {
            direction = WEST;
        }
        else if (direction == WEST)
        {
            direction = NORTH;
        }
        if (hinge == DoorHinges.RIGHT.get())
        {
            direction = direction.getOpposite();
        }
        return direction;
    }

}
