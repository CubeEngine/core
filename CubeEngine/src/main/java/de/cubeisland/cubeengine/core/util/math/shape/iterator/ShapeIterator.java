package de.cubeisland.cubeengine.core.util.math.shape.iterator;

import de.cubeisland.cubeengine.core.util.math.SquareMatrix3;
import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Shape;
import java.util.Iterator;

public abstract class ShapeIterator implements Iterator<Vector3>
{
    private SquareMatrix3 multMatrix;
    protected Shape shape;
    
    ShapeIterator(Shape shape)
    {
        this.shape = shape;
        
        Vector3 rotAngle = shape.getRotationAngle();
        
        if( rotAngle.x != 0 )
        {
            double angle = Math.PI * rotAngle.x / 180.0;
            this.multMatrix = new SquareMatrix3(
                    new Vector3( 1, 0, 0 ),
                    new Vector3( 0, Math.cos( angle ), Math.sin( angle ) ),
                    new Vector3( 0, Math.sin( -angle ), Math.cos( angle ) ) );
        }
        if( rotAngle.y != 0 )
        {
            double angle = Math.PI * rotAngle.y / 180.0;
            SquareMatrix3 rot = new SquareMatrix3(
                    new Vector3( Math.cos( angle ), 0, Math.sin( angle ) ),
                    new Vector3( 0, 1, 0 ),
                    new Vector3( Math.sin( -angle ), 0, Math.cos( angle ) ) );
            if( this.multMatrix == null )
            {
                this.multMatrix = rot;
            }
            else
            {
                this.multMatrix = this.multMatrix.multiply( rot );
            }
        }
        if( rotAngle.z != 0 )
        {
            double angle = Math.PI * rotAngle.z / 180.0;
            SquareMatrix3 rot = new SquareMatrix3(
                    new Vector3( Math.cos( angle ), Math.sin( -angle ), 0 ),
                    new Vector3( Math.sin( angle ), Math.cos( angle ), 0 ),
                    new Vector3( 0, 0, 1 ) );
            if( this.multMatrix == null )
            {
                this.multMatrix = rot;
            }
            else
            {
                this.multMatrix = this.multMatrix.multiply( rot );
            }
        }
        
        SquareMatrix3 scaleMatrix = new SquareMatrix3
        ( 
                new Vector3( shape.getScaleVector().x , 0, 0 ), 
                new Vector3( 0, shape.getScaleVector().y , 0 ), 
                new Vector3( 0, 0, shape.getScaleVector().z )
        );
        
        if(this.multMatrix == null)
        {
            this.multMatrix = scaleMatrix;
        }
        else
        {
            this.multMatrix = this.multMatrix.multiply( scaleMatrix );
        }
    }
    
    protected Vector3 calculatePoint(double x, double y, double z)
    {
        Vector3 diff = new Vector3(this.shape.getPoint().x - this.shape.getCenterOfRotation().x, this.shape.getPoint().y - this.shape.getCenterOfRotation().y, this.shape.getPoint().z - this.shape.getCenterOfRotation().z);
        
        Vector3 point = this.multMatrix.multiply( new Vector3(x + diff.x, y+diff.y, z+diff.z));
        
        return point.add( new Vector3( this.shape.getPoint().x - diff.x, this.shape.getPoint().y - diff.y, this.shape.getPoint().z - diff.z) );
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( "You can't remove any point!" );
    }
}
