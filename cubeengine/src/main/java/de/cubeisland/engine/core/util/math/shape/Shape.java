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
package de.cubeisland.engine.core.util.math.shape;

import de.cubeisland.engine.core.util.math.Vector3;

public interface Shape extends Iterable<Vector3>
{
    public Shape setPoint( Vector3 point );

    public Vector3 getPoint();

    public Shape rotate( Vector3 angle );

    public Shape setCenterOfRotation( Vector3 center );

    public Vector3 getRotationAngle();

    public Vector3 getCenterOfRotation();

    public Shape scale( Vector3 vector );

    public boolean contains( Vector3 point );

    public boolean contains( double x, double y, double z );

    public boolean intersects( Shape other );

    public boolean contains( Shape other );

    public Cuboid getEncircledCuboid();
}
