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
package org.cubeengine.module.core.util;

import org.cubeengine.service.user.MultilingualPlayer;
import org.spongepowered.api.world.Location;

public class LocationUtil
{
    public static Location getBlockBehindWall(MultilingualPlayer user, int maxDistanceToWall, int maxThicknessOfWall)
    {
        // TODO
        return null;
    }

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
}
