/*
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
package org.cubeengine.libcube.util.math.shape;

import com.flowpowered.math.vector.Vector3d;

public interface Shape extends Iterable<Vector3d>
{
    Shape setPoint(Vector3d point);

    Vector3d getPoint();

    Shape rotate(Vector3d angle);

    Shape setCenterOfRotation(Vector3d center);

    Vector3d getRotationAngle();

    Vector3d getCenterOfRotation();

    Shape scale(Vector3d vector);

    boolean contains(Vector3d point);

    boolean contains(double x, double y, double z);

    boolean intersects(Shape other);

    boolean contains(Shape other);

    Cuboid getBoundingCuboid();
}
