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
package de.cubeisland.engine.portals.config;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.portals.Portal;
import de.cubeisland.engine.portals.PortalManager;

public class Destination
{
    public Type type;
    public World world;
    public WorldLocation location;
    public String portal;

    public Destination(Location location)
    {
        this.location = new WorldLocation(location);
        this.world = location.getWorld();
        this.type = Type.LOCATION;
    }

    public Destination(World world)
    {
        this.world = world;
        this.type = Type.WORLD;
    }

    public Destination(Portal portal)
    {
        this.portal = portal.getName();
        this.type = Type.PORTAL;
    }

    protected Destination()
    {}

    public void teleport(User user, PortalManager manager)
    {
        Location loc = null;
        switch (type)
        {
        case PORTAL:
            Portal destPortal = manager.getPortal(portal);
            if (destPortal == null)
            {
                user.sendTranslated("&cDestination portal &6%s&c does not exist!", portal);
                return;
            }
            loc = destPortal.getPortalPos();
            break;
        case WORLD:
            loc = world.getSpawnLocation();
            break;
        case LOCATION:
            loc = location.getLocationIn(world);
            break;
        }
        user.teleport(loc);
        user.sendTranslated("TPed");
    }

    public enum Type
    {
        PORTAL, WORLD, LOCATION;
    }
}
