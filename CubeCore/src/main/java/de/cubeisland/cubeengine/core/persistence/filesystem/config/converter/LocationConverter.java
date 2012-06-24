package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.ConfigurationSection;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

/**
 *
 * @author Faithcaio
 */
public class LocationConverter implements Converter<Location>
{
    private Server server;
    
    public LocationConverter()
    {
        this.server = CubeCore.getInstance().getServer();
    }
    
    public Object from(Location location)
    {
        ConfigurationSection loc = new ConfigurationSection();
        loc.set("world", location.getWorld().getName());
        loc.set("x", location.getX());
        loc.set("y", location.getY());
        loc.set("z", location.getZ());
        loc.set("yaw", location.getYaw());
        loc.set("pitch", location.getPitch());
        return loc;
    }

    public Location to(Object object)
    {
        Map<String, Object> input = ((ConfigurationSection) object).getValues();
        World world = server.getWorld((String) input.get("world"));
        double x = (Double) input.get("x");
        double y = (Double) input.get("y");
        double z = (Double) input.get("z");
        double yaw = (Double) input.get("yaw");
        double pitch = (Double) input.get("pitch");

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }
}