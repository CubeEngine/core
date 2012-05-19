package de.cubeisland.cubeengine.core.util;

import de.cubeisland.libMinecraft.math.Vector2;
import de.cubeisland.libMinecraft.math.Vector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * This is a util class to convert from and to Bukkit objects
 *
 * @author Phillip Schichtel
 */
public final class Convert
{
    private Convert()
    {}

    /**
     * Converts a Location to a Vector3
     *
     * @param loc the Location
     * @return the Vector3
     */
    public static Vector3 toVector3(Location loc)
    {
        if (loc == null)
        {
            return null;
        }
        return new Vector3(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converts a Location to a Vector3
     *
     * @param loc the Location
     * @return the Vector3
     */
    public static Vector3 toBlockVector3(Location loc)
    {
        if (loc == null)
        {
            return null;
        }
        return new Vector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Converts a Location to a Vector2
     *
     * @param loc the Location
     * @return the Vector2
     */
    public static Vector2 toVector2(Location loc)
    {
        if (loc == null)
        {
            return null;
        }
        return new Vector2(loc.getX(), loc.getZ());
    }

    /**
     * Converts a Location to a Vector2
     *
     * @param loc the Location
     * @return the Vector2
     */
    public static Vector2 toBlockVector2(Location loc)
    {
        if (loc == null)
        {
            return null;
        }
        return new Vector2(loc.getBlockX(), loc.getBlockZ());
    }

    /**
     * Converts a World and a Vector3 to a Location
     *
     * @param world the World
     * @param vector the Vector3
     * @return the Location
     */
    public static Location toLocation(World world, Vector3 vector)
    {
        if (world == null || vector == null)
        {
            return null;
        }
        return new Location(world, vector.x, vector.y, vector.z);
    }

    /**
     * Converts a Player and a Vector3 to a Location
     *
     * @param player the Player
     * @param vector the Vector3
     * @return the Location
     */
    public static Location toLocation(Player player, Vector3 vector)
    {
        if (player == null || vector == null)
        {
            return null;
        }
        return toLocation(player.getWorld(), vector);
    }
}
