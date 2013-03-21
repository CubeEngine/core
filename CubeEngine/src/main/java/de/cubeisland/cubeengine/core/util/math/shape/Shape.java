package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public interface Shape extends Iterable<Vector3>
{  
    public Shape setPoint(Vector3 point);
    public Vector3 getPoint();
    
    public Shape rotate(Vector3 angle);
    public Shape setCenterOfRotation( Vector3 center );
    public Vector3 getRotationAngle();
    public Vector3 getCenterOfRotation();
    
    public Shape scale(Vector3 vector);

    public boolean intersects( Shape other );
    public boolean contains( Shape other );
}
