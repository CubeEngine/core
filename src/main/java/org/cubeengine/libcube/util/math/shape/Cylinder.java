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

import java.util.Iterator;
import org.cubeengine.libcube.util.math.MathHelper;
import org.spongepowered.math.vector.Vector3d;

public class Cylinder implements Shape
{
    private final Vector3d point;
    
    private final double radiusX;
    private final double radiusZ;
    private final double height;
    
    private final Vector3d rotationAngle;
    private final Vector3d centerOfRotation;

    public Cylinder( Vector3d point, double radiusX, double radiusZ, double height, Vector3d centerOfRotation, Vector3d rotationAngle )
    {
        this.point = point;
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.height = height;

        this.centerOfRotation = centerOfRotation;
        this.rotationAngle = rotationAngle;
    }

    public Cylinder( Vector3d point, double radiusX, double radiusZ, double height )
    {
        this( point, radiusX, radiusZ, height, new Vector3d( point.getX(), point.getY() + height / 2d, point.getZ()), new Vector3d( 0, 0, 0 ) );
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
    public Shape setPoint( Vector3d point )
    {
        return new Cylinder( point, this.radiusX, this.radiusZ, this.height, this.centerOfRotation, this.rotationAngle );
    }

    @Override
    public Vector3d getPoint()
    {
        return this.point;
    }

    @Override
    public Shape rotate( Vector3d angle )
    {
        return new Cylinder( this.point, this.radiusX, this.radiusZ, this.height, this.centerOfRotation, angle );
    }

    @Override
    public Shape setCenterOfRotation( Vector3d center )
    {
        return new Cylinder( this.point, this.radiusX, this.radiusZ, this.height, center, this.rotationAngle );
    }

    @Override
    public Vector3d getRotationAngle()
    {
        return this.rotationAngle;
    }

    @Override
    public Vector3d getCenterOfRotation()
    {
        return this.centerOfRotation;
    }
    
    @Override
    public Shape scale( Vector3d vector )
    {
        return new Cylinder( this.point, this.radiusX * vector.getX(), this.radiusZ * vector.getZ(), this.height * vector.getY(), this.centerOfRotation, this.rotationAngle );
    }

    @Override
    public Cuboid getBoundingCuboid()
    {
        return new Cuboid
        (
                new Vector3d( this.getPoint().getX() - this.getRadiusX(), this.getPoint().getY(), this.getPoint().getZ() - this.getRadiusZ() ),
                this.getRadiusX() * 2d,
                this.getHeight(),
                this.getRadiusZ() * 2d,
                this.centerOfRotation,
                this.rotationAngle 
        );
    }
    
    @Override
    public boolean contains( Vector3d point )
    {
        return this.contains( point.getX(), point.getY(), point.getZ() );
    }
    
    @Override
    public boolean contains( double x, double y, double z )
    {
        return !(y < this.getPoint().getY() || y > this.getPoint().getY() + this.getHeight()) && MathHelper.pow(
            (x - this.getPoint().getX()) / this.getRadiusX(), 2) + MathHelper.pow( (z - this.getPoint().getZ()) / this.getRadiusZ(), 2 ) < 1;
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
    public Iterator<Vector3d> iterator()
    {
        return new ShapeIterator( this );
    }
}
