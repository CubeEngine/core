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

import java.util.Iterator;

import de.cubeisland.engine.core.util.math.MathHelper;
import de.cubeisland.engine.core.util.math.Vector3;

public class Cylinder implements Shape
{
    private final Vector3 point;
    
    private final double radiusX;
    private final double radiusZ;
    private final double height;
    
    private final Vector3 rotationAngle;
    private final Vector3 centerOfRotation;

    public Cylinder( Vector3 point, double radiusX, double radiusZ, double height, Vector3 centerOfRotation, Vector3 rotationAngle )
    {
        this.point = point;
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.height = height;

        this.centerOfRotation = centerOfRotation;
        this.rotationAngle = rotationAngle;
    }

    public Cylinder( Vector3 point, double radiusX, double radiusZ, double height )
    {
        this( point, radiusX, radiusZ, height, new Vector3( point.x, point.y + height / 2d, point.z ), new Vector3( 0, 0, 0 ) );
    }

    public Cylinder setRadiusX( double radiusX )
    {
        return new Cylinder( this.point, radiusX, this.radiusZ, this.height, this.centerOfRotation, this.rotationAngle );
    }

    public double getRadiusX()
    {
        return this.radiusX;
    }

    public Cylinder setRadiusZ( double radiusZ )
    {
        return new Cylinder( this.point, this.radiusX, radiusZ, this.height, this.centerOfRotation, this.rotationAngle );
    }

    public double getRadiusZ()
    {
        return this.radiusZ;
    }

    public Cylinder setHeight( double height )
    {
        return new Cylinder( this.point, this.radiusX, this.radiusZ, height, this.centerOfRotation, this.rotationAngle );
    }

    public double getHeight()
    {
        return this.height;
    }

    @Override
    public Shape setPoint( Vector3 point )
    {
        return new Cylinder( point, this.radiusX, this.radiusZ, this.height, this.centerOfRotation, this.rotationAngle );
    }

    @Override
    public Vector3 getPoint()
    {
        return this.point;
    }

    @Override
    public Shape rotate( Vector3 angle )
    {
        return new Cylinder( this.point, this.radiusX, this.radiusZ, this.height, this.centerOfRotation, angle );
    }

    @Override
    public Shape setCenterOfRotation( Vector3 center )
    {
        return new Cylinder( this.point, this.radiusX, this.radiusZ, this.height, center, this.rotationAngle );
    }

    @Override
    public Vector3 getRotationAngle()
    {
        return this.rotationAngle;
    }

    @Override
    public Vector3 getCenterOfRotation()
    {
        return this.centerOfRotation;
    }
    
    @Override
    public Shape scale( Vector3 vector )
    {
        return new Cylinder( this.point, this.radiusX * vector.x, this.radiusZ * vector.z, this.height * vector.y, this.centerOfRotation, this.rotationAngle );
    }

    @Override
    public Cuboid getEncircledCuboid()
    {
        return new Cuboid
        (
                new Vector3( this.getPoint().x - this.getRadiusX(), this.getPoint().y, this.getPoint().z - this.getRadiusZ() ),
                this.getRadiusX() * 2d,
                this.getHeight(),
                this.getRadiusZ() * 2d,
                this.centerOfRotation,
                this.rotationAngle 
        );
    }
    
    @Override
    public boolean contains( Vector3 point )
    {
        return this.contains( point.x, point.y, point.z );
    }
    
    @Override
    public boolean contains( double x, double y, double z )
    {
        return !(y < this.getPoint().y || y > this.getPoint().y + this.getHeight()) && MathHelper.pow( (x - this.getPoint().x) / this.getRadiusX(), 2 ) + MathHelper.pow( (z - this.getPoint().z) / this.getRadiusZ(), 2 ) < 1;
    }
    
    @Override
    public boolean contains( Shape other )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    
    @Override
    public boolean intersects( Shape other )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Iterator<Vector3> iterator()
    {
        return new ShapeIterator( this );
    }
}
