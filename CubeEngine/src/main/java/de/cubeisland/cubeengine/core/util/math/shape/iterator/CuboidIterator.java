package de.cubeisland.cubeengine.core.util.math.shape.iterator;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Cuboid;

public class CuboidIterator extends ShapeIterator
{
    private final Cuboid cuboid;
    private double x;
    private double y;
    private double z;

    public CuboidIterator( Cuboid cuboid )
    {
        super(cuboid);
        this.cuboid = cuboid;

        this.x = 0;
        this.y = 0;
        this.z = -1;
    }

    @Override
    public boolean hasNext()
    {
        if( this.x >= this.cuboid.getWidth() - 1 && this.y >= this.cuboid.getHeight() - 1 && this.z >= this.cuboid.getDepth() - 1 )
        {
            return false;
        }
        return true;
    }

    @Override
    public Vector3 next()
    {
        if( this.z < this.cuboid.getDepth() - 1 )
        {
            this.z++;
        }
        else if( this.y < this.cuboid.getHeight() - 1 )
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

        return this.calculatePoint( x, y, z );
    }
}
