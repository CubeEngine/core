package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author CodeInfection
 */
public class Square extends Rectangle
{
    public Square(Vector2 corner, int size)
    {
        this(corner, (double)size);
    }

    public Square(Vector2 corner, double size)
    {
        super(corner, new Vector2(corner.x + size, corner.y + size));
    }
}
