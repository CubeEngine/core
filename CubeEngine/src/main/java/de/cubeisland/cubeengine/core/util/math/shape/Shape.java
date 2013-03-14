package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public interface Shape extends Iterable<Vector3>
{  
    public void setPoint(Vector3 point);
    public Vector3 getPoint();
    
    public void rotate(Vector3 angle);
    public void setCenterOfRotation( Vector3 center );
    public Vector3 getRotationAngle();
    public Vector3 getCenterOfRotation();
    
    public void scale(Vector3 vector);
    public Vector3 getScaleVector();

    public boolean intersects( Shape other );
    public boolean contains( Shape other );
}
