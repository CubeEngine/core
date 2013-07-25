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
import de.cubeisland.engine.core.util.math.SquareMatrix3;
import de.cubeisland.engine.core.util.math.Vector3;

final class ShapeIterator implements Iterator<Vector3>
{
    private final Shape shape;
    
    private final Vector3 cuboidPoint;
    private final double cuboidWidth;
    private final double cuboidHeight;
    private final double cuboidDepth;
    
    private SquareMatrix3 multMatrix;
    
    private double x;
    private double y;
    private double z;

    public ShapeIterator( Shape shape )
    {
        this.shape = shape;

        Cuboid cuboid = shape.getEncircledCuboid();
        this.cuboidPoint = cuboid.getPoint();
        this.cuboidWidth = cuboid.getWidth();
        this.cuboidHeight = cuboid.getHeight();
        this.cuboidDepth = cuboid.getDepth();
        
        this.multMatrix = new SquareMatrix3( 1, 0, 0, 0, 1, 0, 0, 0, 1 );   // identity matrix

        Vector3 rotAngle = shape.getRotationAngle();

        if( rotAngle.x != 0 )
        {
            double angle = MathHelper.degreeToRadians( rotAngle.x );
            this.multMatrix = new SquareMatrix3
            (
                1, 0, 0,
                0, Math.cos( angle ), Math.sin( angle ),
                0, Math.sin( -angle ), Math.cos( angle ) 
            );
        }
        if( rotAngle.y != 0 )
        {
            double angle = MathHelper.degreeToRadians( rotAngle.y );
            this.multMatrix = this.multMatrix.multiply
            (
                new SquareMatrix3
                (
                    Math.cos( angle ), 0, Math.sin( angle ),
                    0, 1, 0,
                    Math.sin( -angle ), 0, Math.cos( angle ) 
                )
            );
        }
        if( rotAngle.z != 0 )
        {
            double angle = MathHelper.degreeToRadians( rotAngle.z );
            this.multMatrix = this.multMatrix.multiply
            ( 
                new SquareMatrix3
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
            if( this.shape.contains( this.x + this.cuboidPoint.x, this.y + this.cuboidPoint.y, this.z + this.cuboidPoint.z) )
            {
                return true;
            }
            this.nextPoint();
        }
    }

    @Override
    public Vector3 next()
    {
        Vector3 nextPoint = this.calculatePoint();
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
    
    private Vector3 calculatePoint()
    {
        double diffx = this.cuboidPoint.x - this.shape.getCenterOfRotation().x;
        double diffy = this.cuboidPoint.y - this.shape.getCenterOfRotation().y;
        double diffz = this.cuboidPoint.z - this.shape.getCenterOfRotation().z;

        Vector3 point = this.multMatrix.multiply( this.x + diffx, this.y + diffy, this.z + diffz );

        return new Vector3( point.x + this.cuboidPoint.x - diffx, point.y + this.cuboidPoint.y - diffy, point.z + this.cuboidPoint.z - diffz );
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( "You can't remove any point!" );
    }
}
