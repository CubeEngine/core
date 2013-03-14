package de.cubeisland.cubeengine.core.util.math.shape;

import de.cubeisland.cubeengine.core.util.math.Vector3;

public class Cube extends Cuboid
{
    public Cube(Vector3 point, int width)
    {
        super(point, width, width, width);
    }
}
