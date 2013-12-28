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
