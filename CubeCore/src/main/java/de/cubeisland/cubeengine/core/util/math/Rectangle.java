package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author CodeInfection
 */
public class Rectangle
{
    private final Vector2 corner1;
    private final Vector2 corner2;

    public Rectangle(Vector2 corner1, Vector2 corner2)
    {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public Vector2 getMinimumPoint()
    {
        return new Vector2(
            Math.min(this.corner1.x, this.corner2.x),
            Math.min(this.corner1.y, this.corner2.y)
        );
    }

    public Vector2 getMaximumPoint()
    {
        return new Vector2(
            Math.max(this.corner1.x, this.corner2.x),
            Math.max(this.corner1.y, this.corner2.y)
        );
    }

    public boolean contains(Vector2 point)
    {
        Vector2 min = this.getMinimumPoint();
        Vector2 max = this.getMaximumPoint();

        return (point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y);
    }
}
