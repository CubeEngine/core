package de.cubeisland.cubeengine.core.util.math.shape.iterator;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Cylinder;

public class CylinderIterator extends ShapeIterator
{
    private double radiusX;
    private double radiusZ;
    private double y;
    private double j;

    public CylinderIterator( Cylinder cylinder )
    {
        super( cylinder );

        this.radiusX = cylinder.getRadiusX();
        this.radiusZ = cylinder.getRadiusZ();
        this.j = 0;
    }

    @Override
    public boolean hasNext()
    {
        if( radiusX >= 0 && radiusZ >= 0 )
        {
            return true;
        }
        return false;
    }

    @Override
    public Vector3 next()
    {
        double blocksPerOval = (this.radiusX + this.radiusZ) * 5;
        double angle = 2 * Math.PI / blocksPerOval;

        double x = Math.cos( this.j * angle ) * this.radiusX + 0.5;
        double y = this.y;
        double z = Math.sin( this.j * angle ) * this.radiusZ + 0.5;        
        j++;
        
        if(j >= blocksPerOval)
        {
            j = 0;
            this.y++;
        }
        if(this.y >= ((Cylinder)this.shape).getHeight() )
        {
            this.y = 0;
            j = 0;
            this.radiusX--;
            this.radiusZ--;
        }
        return this.calculatePoint( x, y, z );
    }
}
