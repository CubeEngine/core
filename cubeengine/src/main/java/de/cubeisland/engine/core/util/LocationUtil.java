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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.user.User;

public class LocationUtil
{
    public static Location getBlockBehindWall(User user, int maxDistanceToWall, int maxThicknessOfWall)
    {
        BlockIterator blockIterator = new BlockIterator(user, maxDistanceToWall + maxThicknessOfWall);
        int curDist = 0;
        boolean passed = false;
        while (blockIterator.hasNext())
        {
            curDist++;
            Block next = blockIterator.next();
            Location loc = new Location(null, 0,0,0);
            if (passed && !next.getType().isSolid())
            {
                MaterialData topData = next.getRelative(BlockFace.UP).getState().getData();
                boolean onHalf = false;
                next.getLocation(loc);
                if (topData.getItemType().isSolid())
                {
                    MaterialData botData = next.getRelative(BlockFace.DOWN).getState().getData();
                    if ((topData instanceof Step || topData instanceof WoodenStep) && (botData instanceof Step || botData instanceof WoodenStep)
                        && BlockUtil.isInvertedStep(topData) && !BlockUtil.isInvertedStep(botData))
                    {
                        onHalf = true;
                    }
                    else if (!next.getRelative(BlockFace.DOWN).getType().isSolid())
                    {
                        loc = loc.add(0, -1, 0);
                    }
                    else
                    {
                        continue;
                    }
                }
                loc = loc.add(0.5, - (onHalf ? 1.5 : 1) ,0.5);
                Location userLoc = user.getLocation();
                loc.setYaw(userLoc.getYaw());
                loc.setPitch(userLoc.getPitch());
                return loc;
            }
            if (curDist >= maxDistanceToWall)
            {
                return null;
            }
            if (next.getType().isSolid() || next.getType() == Material.SUGAR_CANE_BLOCK)
            {
                passed = true;
            }
        }
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
