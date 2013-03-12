package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import java.util.HashSet;

public class Cuboid extends Shape
{
    private double width;
    private double height;
    private double length;

    public Cuboid( double width, double height, double length, Vector3 center )
    {
        this.width = width;
        this.height = height;
        this.length = length;
        this.center = center;
        
        this.createPoints();
    }

    public Cuboid( double width, double height, double length )
    {
        this( width, height, length, new Vector3( 0, 0, 0 ) );
    }

    @Override
    protected void createPoints()
    {
        this.points = new HashSet<Vector3>();

        for(double x = this.width - 3 * this.width / 2d; x <= this.width - this.width / 2; x++)
        {
            for(double y = this.height - 3 * this.height / 2d; y <= this.height - this.height / 2; y++)
            {
                for(double z = this.length - 3 * this.length / 2d; z <= this.length - this.length / 2; z++)
                {
                    this.points.add( new Vector3( x + this.center.x, y + this.center.y, z + this.center.z));
                }
            }
        }
   }

    public double getWidth()
    {
        return this.width;
    }

    public double getHeight()
    {
        return this.height;
    }

    public double getLength()
    {
        return this.length;
    }
}
