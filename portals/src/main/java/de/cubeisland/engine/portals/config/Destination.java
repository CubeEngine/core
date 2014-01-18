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
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.portals.Portal;
import de.cubeisland.engine.portals.PortalManager;

public class Destination
{
    public Type type;
    public ConfigWorld world;
    public WorldLocation location;
    public String portal;

    public Destination(Location location)
    {
        this.location = new WorldLocation(location);
        this.world = new ConfigWorld(CubeEngine.getCore().getWorldManager(), location.getWorld());
        this.type = Type.LOCATION;
    }

    public Destination(World world)
    {
        this.world = new ConfigWorld(CubeEngine.getCore().getWorldManager(), world);
        this.type = Type.WORLD;
    }

    public Destination(Portal portal)
    {
        this.portal = portal.getName();
        this.type = Type.PORTAL;
    }

    protected Destination()
    {}

    public void teleport(final Entity entity, PortalManager manager, boolean safe)
    {
        Location loc = null;
        switch (type)
        {
        case PORTAL:
            Portal destPortal = manager.getPortal(portal);
            if (destPortal == null)
            {
                if (entity instanceof User)
                {
                    ((User)entity).sendTranslated("&cDestination portal &6%s&c does not exist!", portal);
                }
                return;
            }
            loc = destPortal.getPortalPos();
            break;
        case WORLD:
            loc = world.getWorld().getSpawnLocation();
            loc.setX(loc.getBlockX() + 0.5);
            loc.setZ(loc.getBlockZ() + 0.5);
            break;
        case LOCATION:
            loc = location.getLocationIn(world.getWorld());
            break;
        }
        if (entity.isInsideVehicle())
        {
            if (entity instanceof User)
            {
                ((User)entity).sendTranslated("&cYou have to leave your current vehicle to pass a portal!");
            }
            return;
        }
        if (safe && entity instanceof User)
        {
            ((User)entity).safeTeleport(loc, TeleportCause.PLUGIN, false);
        }
        else
        {
            entity.teleport(loc, TeleportCause.PLUGIN);
        }
    }

    public enum Type
    {
        PORTAL, WORLD, LOCATION;
    }
}
