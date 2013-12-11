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
package de.cubeisland.engine.core.world;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.CubeEvent;

public class WorldSetSpawnEvent extends CubeEvent
{
    private World world;
    private Location location;

    public WorldSetSpawnEvent(Core core, World world, int x, int y, int z)
    {
        super(core);
        this.world = world;
        this.location = new Location(world, x, y, z);
    }

    public World getWorld()
    {
        return world;
    }

    public Location getNewLocation()
    {
        return this.location;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
