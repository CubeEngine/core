package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public interface Shape extends Iterable<Vector3>
{  
    public void rotate(float angle, Vector3 vector);
    public void setCenterOfRotation( Vector3 center );
    public float getRotationAngle();
    public Vector3 getRotationVector();
    public Vector3 getCenterOfRotation();
    
    public void scale(Vector3 vector);
    public Vector3 getScaleVector();

    public boolean intersects( Shape other );
    public boolean contains( Shape other );
}
