package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author CodeInfection
 */
public class Cube extends Cuboid
{
    public Cube(Vector3 corner, int size)
    {
        this(corner, (double)size);
    }

    public Cube(Vector3 corner, double size)
    {
        super(corner, new Vector3(corner.x + size, corner.y + size, corner.z + size));
    }
}
