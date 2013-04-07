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

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

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
            if (loc.distanceSquared(originalLoc) > 200 * 200//more than 200 blocks away
                || loc.getY() < 0 //below world
                || loc.getY() > loc.getWorld().getMaxHeight()) //above world 
            {
                return null;
            }
            if (passed && loc.getBlock().getTypeId() == 0)
            {
                if (loc.getBlock().getRelative(BlockFace.UP).getTypeId() != 0)
                {
                    if (loc.getBlock().getRelative(BlockFace.DOWN).getTypeId() == 0)
                    {
                        loc.add(0, -1, 0);
                    }
                    else
                    {
                        continue;
                    }
                }
                loc.setX(loc.getBlockX() + 0.5);
                loc.setY(loc.getBlockY());
                loc.setZ(loc.getBlockZ() + 0.5);
                loc.setYaw(userLocation.getYaw());
                loc.setPitch(userLocation.getPitch());
                return loc;
            }
            else if (loc.getBlock().getTypeId() != 0)
            {
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
            else
            // if Type is AIR
            {
                if (loc.distanceSquared(originalLoc) > maxDistanceToWall * maxDistanceToWall)
                {
                    return null;
                }
            }
        }
    }
}
