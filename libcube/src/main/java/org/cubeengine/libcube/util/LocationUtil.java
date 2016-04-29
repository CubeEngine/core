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
package org.cubeengine.libcube.util;

import java.util.Iterator;
import java.util.Optional;
import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.property.AbstractProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static org.spongepowered.api.data.property.block.MatterProperty.Matter.SOLID;

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

    public static Optional<Location<World>> getBlockBehindWall(Player player, int maxRange, int maxWallThickness)
    {
        Iterator<BlockRayHit<World>> it = BlockRay.from(player).blockLimit(maxRange).iterator();
        Optional<BlockRayHit<World>> end = Optional.empty();

        while (it.hasNext())
        {
            BlockRayHit<World> hit = it.next();
            BlockType blockType = hit.getExtent().getBlockType(hit.getBlockX(), hit.getBlockY(), hit.getBlockZ());
            if (blockType.getProperty(MatterProperty.class).map(AbstractProperty::getValue).orElse(null) == SOLID)
            {
                end = Optional.of(hit);
                break;
            }
        }
        if (!end.isPresent())
        {
            return Optional.empty();
        }

        Vector3d rotation = player.getRotation();
        rotation = Quaterniond.fromAxesAnglesDeg(rotation.getX(), -rotation.getY(), rotation.getZ()).getDirection();
        it = BlockRay.from(end.get().getExtent(), end.get().getPosition()).direction(rotation).blockLimit(maxWallThickness).iterator();
        end = Optional.empty();
        while (it.hasNext())
        {
            BlockRayHit<World> hit = it.next();
            BlockType blockType = hit.getExtent().getBlockType(hit.getBlockX(), hit.getBlockY(), hit.getBlockZ());
            if (blockType.getProperty(MatterProperty.class).map(AbstractProperty::getValue).orElse(null) != SOLID)
            {
                end = Optional.of(hit);
                break;
            }
        }

        return end.map(BlockRayHit::getLocation);
    }
}
