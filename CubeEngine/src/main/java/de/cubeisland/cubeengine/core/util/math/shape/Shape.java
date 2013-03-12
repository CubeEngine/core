package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class Shape implements Iterable<Vector3>
{
    protected Set<Vector3> points;
    protected Vector3 center;
    
    protected abstract void createPoints();

    @Override
    public Iterator<Vector3> iterator()
    {
        return this.points.iterator();
    }

    public Collection<Vector3> getPoints()
    {
        return this.points;
    }

    public void setCenter( Vector3 center )
    {
        if( this.points.isEmpty() )
        {
            Set<Vector3> newPoints = new HashSet<Vector3>();
            Iterator iter = points.iterator();
            while( iter.hasNext() )
            {
                Vector3 v = ( Vector3 ) iter.next();
                newPoints.add( new Vector3( v.x - this.center.x + center.x, v.y - this.center.y + center.y, v.z - this.center.z + center.z ) );
                iter.remove();
            }
            this.points = newPoints;
        }
        this.center = center;
    }

    public Vector3 getCenter()
    {
        return this.center;
    }

    public void scale( float value )
    {
        if( this.points == null || this.points.isEmpty() )
        {
            return;
        }
        Set<Vector3> newPoints = new HashSet<Vector3>();
        Iterator iter = points.iterator();
        while( iter.hasNext() )
        {
            Vector3 v = ( Vector3 ) iter.next();
            newPoints.add( new Vector3( (v.x - this.center.x) * value + this.center.x, (v.y - this.center.y) * value + this.center.y, (v.z - this.center.z) * value + this.center.z ) );
            iter.remove();
        }
        this.points = newPoints;
    }

    public boolean intersects( Shape other )
    {
        throw new UnsupportedOperationException( "not implemented yet" );
    }

    public boolean contains( Shape other )
    {
        throw new UnsupportedOperationException( "not implemented yet" );
    }
}
