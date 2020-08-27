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

import org.cubeengine.libcube.util.math.MathHelper;
import org.spongepowered.math.matrix.Matrix3d;
import org.spongepowered.math.vector.Vector3d;

import java.util.Iterator;

final class ShapeIterator implements Iterator<Vector3d>
{
    private final Shape shape;
    
    private final Vector3d cuboidPoint;
    private final double cuboidWidth;
    private final double cuboidHeight;
    private final double cuboidDepth;
    
    private Matrix3d multMatrix;

    private double x;
    private double y;
    private double z;

    public ShapeIterator( Shape shape )
    {
        this.shape = shape;

        Cuboid cuboid = shape.getBoundingCuboid();
        this.cuboidPoint = cuboid.getPoint();
        this.cuboidWidth = cuboid.getWidth();
        this.cuboidHeight = cuboid.getHeight();
        this.cuboidDepth = cuboid.getDepth();

        this.multMatrix = Matrix3d.IDENTITY;

        Vector3d rotAngle = shape.getRotationAngle();

        if( rotAngle.getX() != 0 )
        {
            double angle = MathHelper.degreeToRadians(rotAngle.getX());
            this.multMatrix = new Matrix3d
            (
                1, 0, 0,
                0, Math.cos( angle ), Math.sin( angle ),
                0, Math.sin( -angle ), Math.cos( angle ) 
            );
        }
        if( rotAngle.getY() != 0 )
        {
            double angle = MathHelper.degreeToRadians( rotAngle.getY() );
            this.multMatrix = this.multMatrix.mul
            (
                new Matrix3d
                (
                    Math.cos( angle ), 0, Math.sin( angle ),
                    0, 1, 0,
                    Math.sin( -angle ), 0, Math.cos( angle ) 
                )
            );
        }
        if( rotAngle.getZ() != 0 )
        {
            double angle = MathHelper.degreeToRadians( rotAngle.getZ() );
            this.multMatrix = this.multMatrix.mul
            ( 
                new Matrix3d
                (
                    Math.cos( angle ), Math.sin( -angle ), 0,
                    Math.sin( angle ), Math.cos( angle ), 0,
                    0, 0, 1 
                )   
            );
        }
        
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public boolean hasNext()
    {
        while(true)
        {
            if( this.x > this.cuboidWidth )
            {
                return false;
            }
            if( this.shape.contains( this.x + this.cuboidPoint.getX(), this.y + this.cuboidPoint.getY(), this.z + this.cuboidPoint.getZ()) )
            {
                return true;
            }
            this.nextPoint();
        }
    }

    @Override
    public Vector3d next()
    {
        Vector3d nextPoint = this.calculatePoint();
        this.nextPoint();
        return nextPoint;
    }
    
    private void nextPoint()
    {
        if( this.z < this.cuboidDepth )
        {
            this.z++;
        }
        else if( this.y < this.cuboidHeight - 1 )
        {
            this.y++;
            this.z = 0;
        }
        else
        {
            this.x++;
            this.y = 0;
            this.z = 0;
        }
    }
    
    private Vector3d calculatePoint()
    {
        double diffx = this.cuboidPoint.getX() - this.shape.getCenterOfRotation().getX();
        double diffy = this.cuboidPoint.getY() - this.shape.getCenterOfRotation().getY();
        double diffz = this.cuboidPoint.getZ() - this.shape.getCenterOfRotation().getZ();

        Vector3d point = this.multMatrix.transform(this.x + diffx, this.y + diffy, this.z + diffz);

        return new Vector3d( point.getX() + this.cuboidPoint.getX() - diffx, point.getY() + this.cuboidPoint.getY() - diffy, point.getZ() + this.cuboidPoint.getZ() -diffz );
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( "You can't remove any point!" );
    }
}
