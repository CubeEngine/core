package de.cubeisland.cubeengine.core.util.math.shape.iterator;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Cuboid;
import java.util.Iterator;

public class CuboidIterator implements Iterator<Vector3>
{
    private final Cuboid cuboid;
    
    private double x;
    private double y;
    private double z;
    
    public CuboidIterator(Cuboid cuboid)
    {
        this.cuboid = cuboid;
        
        this.x = 0;
        this.y = 0;
        this.z = -1;
    }
    
    @Override
    public boolean hasNext()
    {
        if(this.x >= this.cuboid.getWidth() - 1 && this.y >= this.cuboid.getHeight() - 1 && this.z >= this.cuboid.getDepth() - 1)
        {
            return false;
        }
        return true;
    }

    @Override
    public Vector3 next()
    {
        if(this.z < this.cuboid.getDepth() - 1)
        {
            this.z++;
        }
        else if(this.y < this.cuboid.getHeight() - 1)
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
        
        Vector3 scale = this.cuboid.getScaleVector();
        return new Vector3( x * scale.x + this.cuboid.getPoint().x, y * scale.y + this.cuboid.getPoint().y, z * scale.z + this.cuboid.getPoint().z);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException( "You can't remove any point!" );
    }
}
