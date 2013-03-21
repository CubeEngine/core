package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public class CircularCylinder extends Cylinder
{
    public CircularCylinder( Vector3 point, double radius, double height )
    {
        super( point, radius, radius, height );
    }

    public CircularCylinder( Vector3 point, double radius, double height, Vector3 centerOfRadius, Vector3 rotationAngle )
    {
        super( point, radius, radius, height, centerOfRadius, rotationAngle );
    }
}
