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
package org.cubeengine.service.world;

import com.flowpowered.math.vector.Vector3d;
import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.event.CubeEvent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WorldSetSpawnEvent extends CubeEvent
{
    private final World world;
    private final Location<World> location;
    private final Vector3d direction;
    private final Cause cause;

    public WorldSetSpawnEvent(Module core, World world, Location<World> location, Vector3d direction, CommandSource context)
    {
        super(core);
        this.world = world;
        this.location = location;
        this.direction = direction;
        this.cause = Cause.of(context);
    }

    public World getWorld()
    {
        return world;
    }

    public Location getNewLocation()
    {
        return location;
    }

    public Vector3d getRotation()
    {
        return direction;
    }

    @Override
    public Cause getCause()
    {
        return cause;
    }
}
