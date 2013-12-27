package de.cubeisland.engine.portals;

import org.bukkit.Location;

import de.cubeisland.engine.portals.config.PortalConfig;

public class Portal
{
    private String name;
    private PortalConfig config;

    public Portal(String name, PortalConfig config)
    {
        this.name = name;
        this.config = config;
    }

    public String getName()
    {
        return name;
    }

    public boolean has(Location location)
    {
        return location.getWorld() == config.world &&
            isBetween(config.location.from.x, config.location.to.x, location.getBlockX()) &&
            isBetween(config.location.from.y, config.location.to.y, location.getBlockY()) &&
            isBetween(config.location.from.z, config.location.to.z, location.getBlockZ());
    }

    private static boolean isBetween(int a, int b, int x)
    {
        return b > a ? x > a && x < b : x > b && x < a;
    }
}
