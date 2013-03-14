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
    
    private float rotationAngle;
    private Vector3 rotationVector;
    private Vector3 centerOfRotation;
    
    private Vector3 scaleVector;

    public Cuboid( Vector3 point, double width, double height, double depth )
    {
        this.point = point;
        this.width = width;
        this.height = height;
        this.depth = depth;
        
        this.rotationAngle = 0;
        this.rotationVector = new Vector3(0,0,0);
        
        this.scaleVector = new Vector3(1,1,1);
    }

    public void setPoint( Vector3 point )
    {
        this.point = point;
    }
    
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
    public void rotate( float angle, Vector3 vector )
    {
        this.rotationAngle = angle;
        this.rotationVector = vector;
    }

    @Override
    public void setCenterOfRotation( Vector3 center )
    {
        this.centerOfRotation = center;
    }

    @Override
    public float getRotationAngle()
    {
        return this.rotationAngle;
    }

    @Override
    public Vector3 getRotationVector()
    {
        return this.rotationVector;
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
