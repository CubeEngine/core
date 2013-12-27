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

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.user.User;

public class LocationUtil
{
    public static Location getBlockBehindWall(User user, int maxDistanceToWall, int maxThicknessOfWall)
    {
        Location userLocation = user.getLocation();
        double yaw = Math.toRadians(userLocation.getYaw() + 90);
        double pitch = Math.toRadians(-userLocation.getPitch());

        double x = 0.5 * Math.cos(yaw) * Math.cos(pitch);
        double y = 0.5 * Math.sin(pitch);
        double z = 0.5 * Math.sin(yaw) * Math.cos(pitch);
        Vector v = new Vector(x, y, z);
        Location loc = user.getEyeLocation();
        Location originalLoc = user.getEyeLocation();
        Location locBeginWall = null;
        boolean passed = false;
        while (true)
        {
            loc.add(v);
            if (loc.distanceSquared(originalLoc) > maxDistanceToWall * maxDistanceToWall
                || loc.getY() < 0 //below world
                || loc.getY() > loc.getWorld().getMaxHeight()) //above world 
            {
                return null;
            }
            if (passed && (!loc.getBlock().getType().isSolid()))
            {
                if (loc.distanceSquared(locBeginWall) < 1.5) continue; // not yet behind wall
                MaterialData topData = loc.getBlock().getRelative(BlockFace.UP).getState().getData();
                boolean onHalf = false;
                if (topData.getItemType().isSolid())
                {
                    MaterialData botData = loc.getBlock().getRelative(BlockFace.DOWN).getState().getData();
                    if ((topData instanceof Step || topData instanceof WoodenStep) && (botData instanceof Step || botData instanceof WoodenStep) && BlockUtil.isInvertedStep(topData) && !BlockUtil.isInvertedStep(botData))
                    {
                        onHalf = true;
                    }
                    else if (!loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid())
                    {
                        loc.add(0, -1, 0);
                    }
                    else
                    {
                        continue;
                    }
                }
                loc.setX(loc.getBlockX() + 0.5);
                loc.setY(loc.getBlockY() - (onHalf ? 1.5 : 1));
                loc.setZ(loc.getBlockZ() + 0.5);
                loc.setYaw(userLocation.getYaw());
                loc.setPitch(userLocation.getPitch());
                return loc;
            }
            else
            {
                switch (loc.getBlock().getType())
                {
                    case AIR:
                    case WATER:
                    case STATIONARY_WATER:
                        if (loc.distanceSquared(originalLoc) > maxDistanceToWall * maxDistanceToWall)
                        {
                            return null;
                        }
                        break;
                    default:
                        if (!passed)
                        {
                            passed = true;
                            locBeginWall = loc.clone();
                        }
                        if (loc.distanceSquared(locBeginWall) > maxThicknessOfWall * maxThicknessOfWall)
                        {
                            return null;
                        }
                }
            }
        }
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
