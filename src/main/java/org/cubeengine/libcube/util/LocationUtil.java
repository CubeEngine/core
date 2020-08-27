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

import static java.util.stream.Collectors.toList;
import static org.spongepowered.api.util.Direction.UP;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.ServerLocation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LocationUtil
{
    public static long getChunkKey(Location loc)
    {
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        return getChunkKey(chunkX, chunkZ);
    }

    public static long getLocationKey(Location loc)
    {
        int x = loc.getBlockX() & 0x3FFFFFF;
        int y = loc.getBlockY() & 0x1FF;
        int z = loc.getBlockZ() & 0x3FFFFFF;
        return ((((long)x << 26) | z) << 26) | y;
    }

    public static long getChunkKey(int chunkX, int chunkZ)
    {
        return ((long)chunkX << 32) | chunkZ & 0xFFFFFFFFL;
    }

    public static Optional<ServerLocation> getBlockBehindWall(ServerPlayer player, int maxRange, int maxWallThickness)
    {
        Iterator<BlockRayHit> it = Collections.emptyIterator(); // TODO BlockRays?
                //BlockRayHit.from(player).distanceLimit(maxRange + maxWallThickness).iterator();
        boolean wallHit = false;
        int blocks = 0;
        ServerLocation loc = null;
        while (it.hasNext())
        {
            blocks++;
            if (!wallHit && blocks > maxRange)
            {
                break;
            }
            BlockRayHit hit = it.next();
            ServerLocation curLoc = hit.getLocation();
            BlockType blockType = curLoc.getBlockType();
            if (!wallHit)
            {
                if (!canPass(blockType))
                {
                    wallHit = true;
                }
            }
            else
            {
                if (canPass(blockType) && canPass(curLoc.add(UP.asBlockOffset()).getBlockType()))
                {
                    loc = curLoc;
                    break;
                }
            }

        }
        return Optional.ofNullable(loc);
    }

    /**
     * Returns the block in sight.
     * When in fluids returns the first Air or Solid Block
     * else returns the first Solid or Fluid Block
     *
     * @param player the looking player
     * @return the block in sight
     */
    public static ServerLocation getBlockInSight(Player player)
    {
        BlockType headIn = player.getLocation().add(UP.asBlockOffset()).getBlockType();
        List<BlockType> fluidBlocks = Sponge.getRegistry().getCatalogRegistry().getAllOf(FluidType.class).stream()
                .map(FluidType::getDefaultState)
                .map(FluidState::getBlock)
                .map(BlockState::getType)
                .collect(toList());

        boolean headInFluid = fluidBlocks.contains(headIn);
        Iterator<BlockRayHit> it = Collections.emptyIterator();
                // TODO BlockRay.from(player).distanceLimit(500).iterator();
        BlockRayHit next = null;
        while (it.hasNext())
        {
            next = it.next();
            BlockType nextType = next.getLocation().getBlockType();
            if (fluidBlocks.contains(nextType))
            {
                if (!headInFluid)
                {
                    break;
                }
            }
            else if (canPass(nextType))
            {
                if (BlockTypes.AIR.equals(nextType) && headInFluid)
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return next == null ? null : next.getLocation();
    }

    public static boolean canPass(BlockType type)
    {
        return type.getDefaultState().get(Keys.IS_PASSABLE).orElse(false);
    }

    /**
     * Returns the next location up that is not obstructed.
     *
     * @param loc the location to start the search at
     * @return the next non-obstructed location up
     */
    public static ServerLocation getLocationUp(ServerLocation loc)
    {
        if (!canPass(loc.getBlockType()))
        {
            loc = loc.add(UP.asBlockOffset());
        }
        int maxHeight = 256; // TODO loc.getWorld().getDimension().getBuildHeight();
        while (!(canPass(loc.getBlockType()) && canPass(loc.add(UP.asBlockOffset()).getBlockType())) && loc.getY() < maxHeight)
        {
            // TODO half block gaps
            ServerLocation rel = loc.add(UP.asBlockOffset());
            if (rel.getY() == 0)
            {
                break;
            }
            loc = rel;
        }
        return loc;
    }
}
