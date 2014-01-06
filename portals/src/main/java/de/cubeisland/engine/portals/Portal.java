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

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.portals.config.PortalConfig;

public class Portal
{
    private Portals module;
    private PortalManager manager;
    private String name;
    protected final PortalConfig config;

    public Portal(Portals module, PortalManager manager, String name, PortalConfig config)
    {
        this.module = module;
        this.manager = manager;
        this.name = name;
        this.config = config;
    }

    public String getName()
    {
        return name;
    }

    public boolean has(Location location)
    {
        return location.getWorld().getName().equals(config.world) &&
            isBetween(config.location.from.x, config.location.to.x, location.getBlockX()) &&
            isBetween(config.location.from.y, config.location.to.y, location.getBlockY()) &&
            isBetween(config.location.from.z, config.location.to.z, location.getBlockZ());
    }

    private static boolean isBetween(int a, int b, int x)
    {
        return b > a ? x >= a && x <= b : x >= b && x <= a;
    }

    public void teleport(User user)
    {
        if (this.config.destination == null)
        {
            user.sendTranslated("&eThis portal has no destination yet!");
            user.attachOrGet(PortalsAttachment.class, module).setInPortal(true);
        }
        else
        {
            this.config.destination.teleport(user, this.manager);
        }
    }

    public Location getPortalPos()
    {
        BlockVector3 midpoint = this.config.location.to.midpoint(this.config.location.from);
        return new Location(this.config.getWorld(), midpoint.x + 0.5, midpoint.y, midpoint.z + 0.5);
    }
}
