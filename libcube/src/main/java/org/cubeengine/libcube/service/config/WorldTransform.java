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
package org.cubeengine.libcube.service.config;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * A Transform in a world (without the world)
 */
public class WorldTransform
{
    public final double x;
    public final double y;
    public final double z;
    public final double rx;
    public final double ry;
    public final double rz;

    public WorldTransform(double x, double y, double z, double rx, double ry, double rz)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public WorldTransform(Location<World> location, Vector3d rotation)
    {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.rx = rotation.getX();
        this.ry = rotation.getY();
        this.rz = rotation.getZ();
    }

    public final Location<World> getLocationIn(World world)
    {
        return new Location(world, x,y,z);
    }

    public final Vector3d getRotation()
    {
        return new Vector3d(rx, ry, rz);
    }

    public final Transform<World> getTransformIn(World world)
    {
        return new Transform<>(world, new Vector3d(x, y, z), new Vector3d(rx, ry, rz));
    }

    public Vector3d getPosition()
    {
        return new Vector3d(x, y, z);
    }
}
