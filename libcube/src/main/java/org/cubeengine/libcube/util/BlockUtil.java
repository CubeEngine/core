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

import static org.spongepowered.api.block.BlockTypes.ACACIA_DOOR;
import static org.spongepowered.api.block.BlockTypes.ACTIVATOR_RAIL;
import static org.spongepowered.api.block.BlockTypes.AIR;
import static org.spongepowered.api.block.BlockTypes.BIRCH_DOOR;
import static org.spongepowered.api.block.BlockTypes.BROWN_MUSHROOM;
import static org.spongepowered.api.block.BlockTypes.CACTUS;
import static org.spongepowered.api.block.BlockTypes.CARROTS;
import static org.spongepowered.api.block.BlockTypes.COCOA;
import static org.spongepowered.api.block.BlockTypes.DARK_OAK_DOOR;
import static org.spongepowered.api.block.BlockTypes.DEADBUSH;
import static org.spongepowered.api.block.BlockTypes.DETECTOR_RAIL;
import static org.spongepowered.api.block.BlockTypes.FIRE;
import static org.spongepowered.api.block.BlockTypes.FLOWER_POT;
import static org.spongepowered.api.block.BlockTypes.FLOWING_LAVA;
import static org.spongepowered.api.block.BlockTypes.FLOWING_WATER;
import static org.spongepowered.api.block.BlockTypes.GOLDEN_RAIL;
import static org.spongepowered.api.block.BlockTypes.GRAVEL;
import static org.spongepowered.api.block.BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE;
import static org.spongepowered.api.block.BlockTypes.IRON_DOOR;
import static org.spongepowered.api.block.BlockTypes.JUNGLE_DOOR;
import static org.spongepowered.api.block.BlockTypes.LAVA;
import static org.spongepowered.api.block.BlockTypes.LEVER;
import static org.spongepowered.api.block.BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE;
import static org.spongepowered.api.block.BlockTypes.MELON_STEM;
import static org.spongepowered.api.block.BlockTypes.NETHER_WART;
import static org.spongepowered.api.block.BlockTypes.PORTAL;
import static org.spongepowered.api.block.BlockTypes.POTATOES;
import static org.spongepowered.api.block.BlockTypes.POWERED_COMPARATOR;
import static org.spongepowered.api.block.BlockTypes.POWERED_REPEATER;
import static org.spongepowered.api.block.BlockTypes.PUMPKIN_STEM;
import static org.spongepowered.api.block.BlockTypes.RAIL;
import static org.spongepowered.api.block.BlockTypes.REDSTONE_TORCH;
import static org.spongepowered.api.block.BlockTypes.REDSTONE_WIRE;
import static org.spongepowered.api.block.BlockTypes.RED_FLOWER;
import static org.spongepowered.api.block.BlockTypes.RED_MUSHROOM;
import static org.spongepowered.api.block.BlockTypes.REEDS;
import static org.spongepowered.api.block.BlockTypes.SAND;
import static org.spongepowered.api.block.BlockTypes.SAPLING;
import static org.spongepowered.api.block.BlockTypes.SKULL;
import static org.spongepowered.api.block.BlockTypes.SNOW;
import static org.spongepowered.api.block.BlockTypes.SPRUCE_DOOR;
import static org.spongepowered.api.block.BlockTypes.STANDING_SIGN;
import static org.spongepowered.api.block.BlockTypes.STONE_PRESSURE_PLATE;
import static org.spongepowered.api.block.BlockTypes.TALLGRASS;
import static org.spongepowered.api.block.BlockTypes.TORCH;
import static org.spongepowered.api.block.BlockTypes.TRIPWIRE;
import static org.spongepowered.api.block.BlockTypes.TRIPWIRE_HOOK;
import static org.spongepowered.api.block.BlockTypes.UNLIT_REDSTONE_TORCH;
import static org.spongepowered.api.block.BlockTypes.UNPOWERED_COMPARATOR;
import static org.spongepowered.api.block.BlockTypes.UNPOWERED_REPEATER;
import static org.spongepowered.api.block.BlockTypes.VINE;
import static org.spongepowered.api.block.BlockTypes.WALL_SIGN;
import static org.spongepowered.api.block.BlockTypes.WATER;
import static org.spongepowered.api.block.BlockTypes.WATERLILY;
import static org.spongepowered.api.block.BlockTypes.WEB;
import static org.spongepowered.api.block.BlockTypes.WHEAT;
import static org.spongepowered.api.block.BlockTypes.WOODEN_DOOR;
import static org.spongepowered.api.block.BlockTypes.WOODEN_PRESSURE_PLATE;
import static org.spongepowered.api.block.BlockTypes.YELLOW_FLOWER;
import static org.spongepowered.api.util.Direction.DOWN;
import static org.spongepowered.api.util.Direction.EAST;
import static org.spongepowered.api.util.Direction.NORTH;
import static org.spongepowered.api.util.Direction.SOUTH;
import static org.spongepowered.api.util.Direction.UP;
import static org.spongepowered.api.util.Direction.WEST;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.manipulator.mutable.block.AttachedData;
import org.spongepowered.api.data.manipulator.mutable.block.PortionData;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.storage.ChunkLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    public static Collection<Location<World>> getAttachedBlocks(Location<World> block)
    {
        Collection<Location<World>> blocks = new HashSet<>();
        for (org.spongepowered.api.util.Direction bf : BLOCK_FACES)
        {
            Optional<AttachedData> attached = block.getRelative(bf).get(AttachedData.class);
            if (attached.isPresent())
            {
                // TODO
                /*
                if (attached.get().getFace().opposite().equals(bf))
                {
                    blocks.add(block.getRelative(bf));
                }
                */
            }
        }
        return blocks;
    }

    public static Set<BlockType> DETACHABLE_FROM_BELOW = new HashSet<>(Arrays.asList(BROWN_MUSHROOM, CARROTS, DEADBUSH,
                                                                                     DETECTOR_RAIL, POTATOES, WHEAT,
                                                                                     POWERED_REPEATER,
                                                                                     UNPOWERED_REPEATER, FLOWER_POT,
                                                                                     IRON_DOOR, LEVER, TALLGRASS,
                                                                                     MELON_STEM, NETHER_WART, PORTAL,
                                                                                     GOLDEN_RAIL, ACTIVATOR_RAIL,
                                                                                     POWERED_COMPARATOR,
                                                                                     UNPOWERED_COMPARATOR,
                                                                                     HEAVY_WEIGHTED_PRESSURE_PLATE,
                                                                                     LIGHT_WEIGHTED_PRESSURE_PLATE,
                                                                                     PUMPKIN_STEM, RAIL, RED_MUSHROOM,
                                                                                     RED_FLOWER, REDSTONE_WIRE,
                                                                                     REDSTONE_TORCH,
                                                                                     UNLIT_REDSTONE_TORCH, SAPLING,
                                                                                     STANDING_SIGN, WALL_SIGN, SKULL,
                                                                                     SNOW, STONE_PRESSURE_PLATE, TORCH,
                                                                                     TRIPWIRE, WATERLILY, WOODEN_DOOR,
                                                                                     WOODEN_PRESSURE_PLATE,
                                                                                     YELLOW_FLOWER, REEDS, CACTUS, SAND,
                                                                                     GRAVEL));

    public static boolean isDetachableFromBelow(BlockType mat)
    {
        return DETACHABLE_FROM_BELOW.contains(mat);
    }

    public static Collection<Location<World>> getDetachableBlocksOnTop(Location<World> block)
    {
        Collection<Location<World>> blocks = new HashSet<>();
        Location<World> onTop = block.getRelative(UP);
        while (isDetachableFromBelow(onTop.getBlockType()))
        {
            blocks.add(onTop);
            for (Location<World> attachedBlock : getAttachedBlocks(onTop))
            {
                blocks.add(attachedBlock);
                blocks.addAll(getDetachableBlocksOnTop(attachedBlock));
            }
            onTop = onTop.getRelative(UP);
        }
        return blocks;
    }

    public static Collection<Location<World>> getDetachableBlocks(Location<World> block)
    {
        Collection<Location<World>> blocks = new HashSet<>();

        for (Location<World> attachedBlock : getAttachedBlocks(block))
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
            BlockType type = block.getRelative(face).getBlockType();
            if (type == WATER || type == FLOWING_WATER)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isInvertedStep(Location location)
    {
        Optional<PortionData> data = location.get(PortionData.class);
        if (data.isPresent())
        {
            return data.get().type().get() == PortionTypes.TOP;
        }
        return false;
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

    public static Location<World> getHighestBlockAt(Location<World> loc)
    {
        return getHighestBlockAt(loc.getExtent(), loc.getBlockX(), loc.getBlockZ());
    }

    @SuppressWarnings("deprecation")
    public static Location<World> getHighestBlockAt(World world, final int x, final int z)
    {
        int y = world.getDimension().getBuildHeight() - 1;

        while (world.getBlockType(x, y, z) == AIR && y > 0)
        {
            --y;
        }

        return world.getLocation(x, y, z);
    }

    public static Optional<Chunk> getChunk(Location<World> block, Game game)
    {
        ChunkLayout cl = game.getServer().getChunkLayout();
        return block.getExtent().getChunk(cl.toChunk(block.getBlockPosition()).get());
    }

    public static Direction getOtherDoorDirection(Direction direction, Hinge hinge)
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
        if (hinge == Hinges.RIGHT)
        {
            direction = direction.getOpposite();
        }
        return direction;
    }

    public static void applyVolume(ArchetypeVolume volume, Location<World> at, Cause cause, PluginContainer plugin)
    {
        new LagFreeBlockWorker(volume, at, cause, plugin).run();
    }

    private static class LagFreeBlockWorker
    {
        private ArchetypeVolume volume;
        private Location<World> at;
        private Cause cause;
        private PluginContainer plugin;

        public LagFreeBlockWorker(ArchetypeVolume volume, Location<World> at, Cause cause, PluginContainer plugin)
        {
            this.volume = volume;
            this.at = at;
            this.cause = cause;
            this.plugin = plugin;
        }

        private final int xMin = volume.getBlockMin().getX();
        private final int yMin = volume.getBlockMin().getY();
        private final int zMin = volume.getBlockMin().getZ();
        private final int xMax = volume.getBlockMax().getX();
        private final int yMax = volume.getBlockMax().getY();
        private final int zMax = volume.getBlockMax().getZ();

        private int z = zMin;
        private int y = yMin;
        private int x = xMin;

        private static final long MSPT = 50; // 20 TPS = 1T/50MS
        private int tickPercentage = 60;

        public void run()
        {
            if (!processVolume())
            {
                Sponge.getScheduler().createTaskBuilder().delayTicks(1).execute(this::run).name("CE-BlockWorker").submit(plugin);
            }
        }

        private boolean processVolume()
        {
            // TODO entities?
            long msLimit = MSPT * tickPercentage / 100;
            long start = System.currentTimeMillis();
            int i = 0;
            for (; z <= zMax; z++)
            {
                for (; y <= yMax; y++)
                {
                    for (; x <= xMax; x++)
                    {
                        i++;
                        at.getExtent().setBlock(at.getBlockX() + x, at.getBlockY() + y, at.getBlockZ() + z, volume.getBlock(x,y,z), BlockChangeFlag.NONE, cause);
                        Optional<TileEntityArchetype> te = volume.getTileEntityArchetype(x, y, z);
                        te.ifPresent(tileEntityArchetype -> tileEntityArchetype.apply(at.add(x, y, z), cause));
                        if (i % 100 == 0)
                        {
                            if ((System.currentTimeMillis() - start) > msLimit)
                            {
                                return x != xMax || y != yMax || z != zMax;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
}
