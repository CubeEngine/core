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

public class Sphere implements Shape
{
    private Vector3d point;
    private double radius;
    
    private Vector3d centerOfRotation;
    private Vector3d rotationAngle;
    
    public Sphere(Vector3d point, double radius)
    {
        this(point, radius, point, new Vector3d(0,0,0));
    }
    
    public Sphere(Vector3d point, double radius, Vector3d centerOfRotation, Vector3d rotationAngle)
    {
        this.point = point;
        this.radius = radius;
        
        this.centerOfRotation = centerOfRotation;
        this.rotationAngle = rotationAngle;
    }
    
    public Sphere setRadius(double radius)
    {
        return new Sphere(this.point, radius, this.centerOfRotation, this.rotationAngle);
    }
    
    public double getRadius()
    {
        return this.radius;
    }
    
    @Override
    public Shape setPoint( Vector3d point )
    {
        return new Sphere(point, this.radius, this.centerOfRotation, this.rotationAngle);
    }

    @Override
    public Vector3d getPoint()
    {
        return this.point;
    }

    @Override
    public Shape rotate( Vector3d angle )
    {
        return new Sphere(this.point, this.radius, this.centerOfRotation, angle);
    }

    @Override
    public Shape setCenterOfRotation( Vector3d center )
    {
        return new Sphere(this.point, this.radius, center, this.rotationAngle);
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
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean contains( Vector3d point )
    {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains( double x, double y, double z )
    {
        return MathHelper.pow( this.point.x() - x, 2 ) + MathHelper.pow( this.point.y() - y, 2 ) + MathHelper.pow( this.point.z() -z, 2 ) < this.radius * this.radius;
    }

    @Override
    public boolean intersects( Shape other )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains( Shape other )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Cuboid getBoundingCuboid()
    {
        return new Cuboid
        (
                new Vector3d( this.getPoint().x() - this.getRadius(), this.getPoint().x() - this.getRadius(), this.getPoint().x() - this.getRadius() ),
                this.getRadius() * 2d,
                this.getRadius() * 2d,
                this.getRadius() * 2d,
                this.centerOfRotation,
                this.rotationAngle 
        );
    }

    @Override
    public Iterator<Vector3d> iterator()
    {
        return new ShapeIterator(this);
    }
    
}
