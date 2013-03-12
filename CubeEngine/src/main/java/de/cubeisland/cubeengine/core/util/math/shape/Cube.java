package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public class Cube extends Cuboid
{
    public Cube(int width)
    {
        super(width, width, width);
    }
    
    public Cube(int width, Vector3 center)
    {
        super(width, width, width, center);
    }
}
