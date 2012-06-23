package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

/**
 *
 * @author Faithcaio
 */
public class LocationConverter  implements IConverter<Location>
{
    public Object from(Location location)
    {
        LinkedHashMap<String,Object> loc = new LinkedHashMap<String, Object>();
        loc.put("world", location.getWorld().getName());
        loc.put("x", location.getX());
        loc.put("y", location.getY());
        loc.put("z", location.getZ());
        loc.put("yaw", location.getYaw());
        loc.put("pitch", location.getPitch());
        return loc;
    }

    public Location to(Object object)
    {
        Server server = CubeCore.getInstance().getServer();
        Map<String,Object> input = (Map<String,Object>)object;
        World world = server.getWorld((String)input.get("world"));
        double x = (Double) input.get("x");
        double y = (Double) input.get("y");
        double z = (Double) input.get("z");
        float yaw = (Float) input.get("yaw");
        float pitch = (Float) input.get("pitch");                

        return new Location(world, x, y, z, yaw, pitch);
    }
}