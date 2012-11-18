package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LocationUtil
{

    public static Location getBlockBehindWall(User user)
    {
        double yaw = Math.toRadians(user.getLocation().getYaw() + 90);
        double pitch = Math.toRadians(-user.getLocation().getPitch());

        double x = 0.2 * Math.cos(yaw) * Math.cos(pitch);
        double y = 0.2 * Math.sin(pitch);
        double z = 0.2 * Math.sin(yaw) * Math.cos(pitch);
        Vector v = new Vector(x, y, z);
        Location loc = user.getLocation();
        Location originalLoc = user.getLocation();
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
            if (passed)
            {
                if (loc.getBlock().getTypeId() == 0)
                {
                    return loc;
                }
            }
            else if (loc.getBlock().getTypeId() != 0)
            {
                passed = true;
            }
        }
    }
}