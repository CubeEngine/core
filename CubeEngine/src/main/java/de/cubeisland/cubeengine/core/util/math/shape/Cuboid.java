package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.iterator.CuboidIterator;
import java.util.Iterator;

public class Cuboid implements Shape
{
    private Vector3 point;
    
    private double width;
    private double height;
    private double depth;
    
    private Vector3 rotationAngle;
    private Vector3 centerOfRotation;
    
    private Vector3 scaleVector;

    public Cuboid( Vector3 point, double width, double height, double depth )
    {
        this.point = point;
        this.width = width;
        this.height = height;
        this.depth = depth;
        
        this.rotationAngle = new Vector3(0,0,0);
        this.centerOfRotation = new Vector3(this.point.x + width / 2, this.point.y + height / 2, this.point.z + depth / 2);
        
        this.scaleVector = new Vector3(1,1,1);
    }

    @Override
    public void setPoint( Vector3 point )
    {
        this.point = point;
    }
    
    @Override
    public Vector3 getPoint()
    {
        return this.point;
    }
    
    public void setWidth(double width)
    {
        this.width = width;
    }
    
    public double getWidth()
    {
        return this.width;
    }
    
    public void setHeight(double height)
    {
        this.height = height;
    }
    
    public double getHeight()
    {
        return this.height;
    }
    
    public void setDepth(double depth)
    {
        this.depth = depth;
    }
    
    public double getDepth()
    {
        return this.depth;
    }

    @Override
    public void rotate( Vector3 angle )
    {
        this.rotationAngle = angle;
    }

    @Override
    public void setCenterOfRotation( Vector3 center )
    {
        this.centerOfRotation = center;
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
    public void scale( Vector3 vector )
    {
        this.scaleVector = vector;
    }

    @Override
    public Vector3 getScaleVector()
    {
        return this.scaleVector;
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
    public Iterator<Vector3> iterator()
    {
        return new CuboidIterator( this );
    }
}
