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

import org.spongepowered.math.vector.Vector3d;

import java.util.Iterator;

public class Cuboid implements Shape
{
    private Vector3d point;
    
    private double width;
    private double height;
    private double depth;
    
    private Vector3d rotationAngle;
    private Vector3d centerOfRotation;

    public Cuboid( Vector3d point, double width, double height, double depth )
    {
        this.point = point;
        this.width = width;
        this.height = height;
        this.depth = depth;
        
        this.centerOfRotation = new Vector3d(this.point.x() + width / 2, this.point.y() + height / 2, this.point.z() + depth / 2);
        this.rotationAngle = new Vector3d(0, 0, 0);
    }

    
    public Cuboid( Vector3d point, double width, double height, double depth, Vector3d centerOfRotation, Vector3d rotationAngle)
    {
        this.point = point;
        this.width = width;
        this.height = height;
        this.depth = depth;
        
        this.centerOfRotation = centerOfRotation;
        this.rotationAngle = rotationAngle;
    }

    public Cuboid(Vector3d point, Vector3d size)
    {
        this(point, size.x(), size.y(), size.z());
    }

    @Override
    public Shape setPoint( Vector3d point )
    {
        return new Cuboid(point, this.width, this.height, this.depth, this.centerOfRotation, this.rotationAngle);
    }
    
    @Override
    public Vector3d getPoint()
    {
        return this.point;
    }
    
    public Cuboid setWidth(double width)
    {
        return new Cuboid(this.point, width, this.height, this.depth, this.centerOfRotation, this.rotationAngle);
    }
    
    public double getWidth()
    {
        return this.width;
    }
    
    public Cuboid setHeight(double height)
    {
        return new Cuboid(this.point, this.width, height, this.depth, this.centerOfRotation, this.rotationAngle);
    }
    
    public double getHeight()
    {
        return this.height;
    }
    
    public Cuboid setDepth(double depth)
    {
        return new Cuboid(this.point, this.width, this.height, depth, this.centerOfRotation, this.rotationAngle);
    }
    
    public double getDepth()
    {
        return this.depth;
    }

    @Override
    public Shape rotate( Vector3d angle )
    {
        return new Cuboid(this.point, this.width, this.height, this.depth, this.centerOfRotation, angle);
    }

    @Override
    public Shape setCenterOfRotation( Vector3d center )
    {
        return new Cuboid(this.point, this.width, this.height, this.depth, center, this.rotationAngle);
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
        return new Cuboid(this.point, this.width * vector.x(), this.height * vector.y(), this.depth * vector.z(), this.centerOfRotation, this.rotationAngle);
    }

    @Override
    public Cuboid getBoundingCuboid()
    {
        return this;
    }
    
    @Override
    public boolean contains( Vector3d point )
    {
        return this.contains( point.x(), point.y(), point.z() );
    }


    /**
     * Gets a Vektor3 pointing to the minium point
     *
     * @return the Vektor pointing to the minimum point
     */
    public Vector3d getMinimumPoint()
    {
        return new Vector3d(
            Math.min(this.point.x(), this.point.x() + this.width),
            Math.min(this.point.y(), this.point.y() + this.height),
            Math.min(this.point.z(), this.point.z() + this.depth));
    }

    /**
     * Gets a Vektor3 pointing to the maximum point
     *
     * @return the Vektor pointing to the maximum point
     */
    public Vector3d getMaximumPoint()
    {
        return new Vector3d(
                Math.max(this.point.x(), this.point.x() + this.width),
                Math.max(this.point.y(), this.point.y() + this.height),
                Math.max(this.point.z(), this.point.z() + this.depth));
    }

    @Override
    public boolean contains( double x, double y, double z )
    {
        return !(   
            x < this.getPoint().x() ||
            x > this.getPoint().x() + this.getWidth() ||
            y < this.getPoint().y() ||
            y > this.getPoint().y() + this.getHeight() ||
            z < this.getPoint().z() ||
            z > this.getPoint().z() + this.getDepth()
        );
    }
    
    @Override
    public boolean contains( Shape other )
    {
        return other instanceof Cuboid && this.contains((Cuboid)other);
    }
    
    private boolean contains(Cuboid other)
    {
        return 
        (
            this.getPoint().y() + this.getHeight() >= other.getPoint().y() + other.getHeight() &&    // this.top > other.top
            this.getPoint().y() <= other.getPoint().y() &&                                           // this.bottom < other.bottom
            this.getPoint().x() <= other.getPoint().x() &&                                           // this.left < other.left
            this.getPoint().x() + this.getWidth() >= other.getPoint().x() + other.getWidth() &&      // this.right > other.right
            this.getPoint().z() <= other.getPoint().z() &&                                           // this.front < other.front
            this.getPoint().z() + this.getDepth() >= other.getPoint().z() + other.getDepth()         // this.back > other.back
        );
    }
    
    @Override
    public boolean intersects( Shape other )
    {
        return other instanceof Cuboid && this.intersects((Cuboid)other);
    }
    
    private boolean intersects(Cuboid other)
    { 
        return !(                                                           // invert it
            this.getPoint().y() + this.getHeight() < other.getPoint().y() ||    // this.top < other.bottom
            this.getPoint().y() > other.getPoint().y() + other.getHeight()  ||  // this.bottom > other.top
            this.getPoint().x() > other.getPoint().x() + other.getWidth() ||    // this.left > other.right
            this.getPoint().x() + this.getWidth() < other.getPoint().x() ||     // this.right < other.left
            this.getPoint().z() > other.getPoint().z() + other.getDepth() ||    // this.front > other.back
            this.getPoint().z() + this.getDepth() < other.getPoint().z() ||     // this.back < other.front
            this.contains( other )
        );
    }
    
    @Override
    public Iterator<Vector3d> iterator()
    {
        return new ShapeIterator( this );
    }
}
