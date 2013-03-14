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
    
    private boolean intersects(Cuboid other)
    { 
        return !(                                                           // invert it
            this.getPoint().y + this.getHeight() < other.getPoint().y ||    // this.top < other.bottom
            this.getPoint().y > other.getPoint().y + other.getHeight()  ||  // this.bottom > other.top
            this.getPoint().x > other.getPoint().x + other.getWidth() ||    // this.left > other.right
            this.getPoint().x + this.getWidth() < other.getPoint().x ||     // this.right < other.left
            this.getPoint().z > other.getPoint().z + other.getDepth() ||    // this.front > other.back
            this.getPoint().z + this.getDepth() < other.getPoint().z ||     // this.back < other.front
            this.contains( other )
        );
    }

    @Override
    public boolean intersects( Shape other )
    {
        if(other instanceof Cuboid)
        {
            return this.intersects((Cuboid) other);
        }
        return false;  
    }

    private boolean contains(Cuboid other)
    {
        return 
        (
            this.getPoint().y + this.getHeight() > other.getPoint().y + other.getHeight() &&    // this.top > other.top
            this.getPoint().y < other.getPoint().y &&                                           // this.bottom < other.bottom
            this.getPoint().x < other.getPoint().x &&                                           // this.left < other.left
            this.getPoint().x + this.getWidth() > other.getPoint().x + other.getWidth() &&      // this.right > other.right
            this.getPoint().z < other.getPoint().z &&                                           // this.front < other.front
            this.getPoint().z + this.getDepth() > other.getPoint().z + other.getDepth()         // this.back > other.back
        );
    }
    
    @Override
    public boolean contains( Shape other )
    {
        if(other instanceof Cuboid)
        {
            return this.contains((Cuboid) other);
        }
        return false; 
    }

    @Override
    public Iterator<Vector3> iterator()
    {
        return new CuboidIterator( this );
    }
}