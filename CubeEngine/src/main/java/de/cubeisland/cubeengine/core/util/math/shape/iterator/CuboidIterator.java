package de.cubeisland.cubeengine.core.util.math.shape.iterator;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Cuboid;
import java.util.Iterator;

public class CuboidIterator implements Iterator<Vector3>
{
    private final Cuboid cuboid;
    
    private final Vector3 point;
    
    private double x;
    private double y;
    private double z;
    
    public CuboidIterator(Cuboid cuboid)
    {
        this.cuboid = cuboid;
        this.point = this.cuboid.getPoint();
        this.x = this.cuboid.getPoint().x;
        this.y = this.cuboid.getPoint().y;
        this.z = this.cuboid.getPoint().z - 1;
    }
    
    @Override
    public boolean hasNext()
    {
        if(this.x >= this.point.x + this.cuboid.getWidth() - 1 && this.y >= this.point.y + this.cuboid.getHeight() - 1 && this.z >= this.point.z + this.cuboid.getDepth() - 1) // fehlt was ;)
        {
            return false;
        }
        return true;
    }

    @Override
    public Vector3 next()
    {
        if(this.z < this.point.z + this.cuboid.getDepth() - 1)
        {
            this.z++;
        }
        else if(this.y < this.point.y + this.cuboid.getHeight() - 1)
        {
            this.y++;
            this.z = this.point.z;
        }
        else
        {
            this.x++;
            this.y = this.point.y;
            this.z = this.point.z;
        }
        
        Vector3 scale = this.cuboid.getScaleVector();
        return new Vector3( (x - this.point.x) * scale.x + this.point.x, (y - this.point.y) * scale.y + this.point.y, (z - this.point.z) * scale.z + this.point.z);
    }

    @Override
    public void remove()
    {
    
    }
}
