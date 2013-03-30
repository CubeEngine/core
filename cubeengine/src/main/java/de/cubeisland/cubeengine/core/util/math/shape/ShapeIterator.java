package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.MathHelper;
import de.cubeisland.cubeengine.core.util.math.SquareMatrix3;
import de.cubeisland.cubeengine.core.util.math.Vector3;
import java.util.Iterator;

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
