/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.util.math;

/**
 * Represents a Rectangle specified by two corners.
 */
public class Rectangle
{
    private final Vector2 corner1;
    private final Vector2 corner2;

    /**
     * Creates a Rectangle with the 2 Vectors
     *
     * @param corner1 Vektor to the first corner
     * @param corner2 Vektor to the second corner
     */
    public Rectangle(Vector2 corner1, Vector2 corner2)
    {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    /**
     * Gets a Vektor2 pointing to the minium point
     *
     * @return the Vektor pointing to the minimum point
     */
    public Vector2 getMinimumPoint()
    {
        return new Vector2(
            Math.min(this.corner1.x, this.corner2.x),
            Math.min(this.corner1.y, this.corner2.y));
    }

    /**
     * Gets a Vektor2 pointing to the maximum point
     *
     * @return the Vektor pointing to the maximum point
     */
    public Vector2 getMaximumPoint()
    {
        return new Vector2(
            Math.max(this.corner1.x, this.corner2.x),
            Math.max(this.corner1.y, this.corner2.y));
    }

    /**
     * Check whether the given point is in this Rectangle
     *
     * @param point the point to check
     * @return whether the point is in the cuboid or not
     */
    public boolean contains(Vector2 point)
    {
        Vector2 min = this.getMinimumPoint();
        Vector2 max = this.getMaximumPoint();

        return (point.x >= min.x && point.x <= max.x
            && point.y >= min.y && point.y <= max.y);
    }
}
