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
 * This class represents a 2D vector.
 */
public class Vector2
{
    public final double x;
    public final double y;

    /**
     * Creates a new Vector2 with a pair of int
     *
     * @param x the x value
     * @param y the y value
     */
    public Vector2(int x, int y)
    {
        this((double)x, (double)y);
    }

    /**
     * Creates a new Vector2 with a pair of double
     *
     * @param x the x value
     * @param y the y value
     */
    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks whether an other Vector2 ist orthogonal to this one
     *
     * @param other the other Vector2
     * @return whether the other Vector2 is orthogonal to this one
     */
    public boolean isOrthogonal(Vector2 other)
    {
        return (this.dot(other) == 0.0);
    }

    /**
     * Checks whether an other Vector2 ist parallel to this one
     *
     * @param other the other Vector2
     * @return whether the other Vector2 is parallel to this one
     */
    public boolean isParallel(Vector2 other)
    {
        return (this.x / other.x == this.y / other.y);
    }

    /**
     * Returns the scalar product of this vector and the other
     *
     * @param other the second vector to multiply with
     * @return the scalar product
     */
    public double dot(Vector2 other)
    {
        return (this.x * other.x + this.y * other.y);
    }

    /**
     * Returns the sum of an other Vector2 and this one as a Vector2
     *
     * @param other the Vector2 to add
     * @return the new Vector
     */
    public Vector2 add(Vector2 other)
    {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    /**
     * Returns the difference between an other Vector2 and this one as a Vector2
     *
     * @param other the Vector2 to subtract
     * @return the new Vector
     */
    public Vector2 subtract(Vector2 other)
    {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public Vector2 multiply(int n)
    {
        return this.multiply((double)n);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public Vector2 multiply(double n)
    {
        return new Vector2(this.x * n, this.y * n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public Vector2 divide(int n)
    {
        return this.divide((double)n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public Vector2 divide(double n)
    {
        return new Vector2(this.x / n, this.y / n);
    }

    /**
     * Returns the squared length
     *
     * @return the length²
     */
    public double squaredLength()
    {
        return (Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    /**
     * Returns the length
     *
     * @return the length
     */
    public double length()
    {
        return Math.sqrt(this.squaredLength());
    }

    /**
     * Returns the distance between an other Vector2 and this one as a Vector2
     *
     * @param other the Vector2 to subtract from
     * @return the new Vector
     */
    public Vector2 distanceVector(Vector2 other)
    {
        return other.subtract(this);
    }

    /**
     * Returns the squared distance between an other Vector2 and this one
     *
     * @param other the Vector2 to get the distance from
     * @return the squared distance
     */
    public double squaredDistance(Vector2 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    /**
     * Returns the distance between an other Vector2 and this one
     *
     * @param other the Vector2 to get the distance from
     * @return the distance
     */
    public double distance(Vector2 other)
    {
        return this.distanceVector(other).length();
    }

    /**
     * Returns the angle of the other Vector to this one
     *
     * @param other  the other Vector2 to calculate the angle to
     * @return the angle between the vectors
     */
    public double crossAngle(Vector2 other)
    {
        return Math.acos(this.dot(other) / (this.length() * other.length()));
    }

    /**
     * Returns the normalized Vector
     *
     * @return the normalized Vector2
     */
    public Vector2 normalize()
    {
        return this.divide(this.length());
    }

    /**
     * Returns a Vector2 to the midpoint between this Vector and the other
     *
     * @param other the other Vector2
     * @return the midpoint
     */
    public Vector2 midpoint(Vector2 other)
    {
        return this.add(other.subtract(this).divide(2));
    }

    /**
     * Returns whether the Object o equals this Vector
     *
     * @param o an Object
     * @return whether o is the same Vector as this one
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (!(o instanceof Vector2))
        {
            return false;
        }

        Vector2 other = (Vector2)o;

        return (this.x == other.x && this.y == other.y);
    }

    /**
     * Returns a HashCode representing this Vector
     *
     * @return the HashCode of this Vector2
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    /**
     * Returns a String representing this Vector
     *
     * @return the String representing this Vector2
     */
    @Override
    public String toString()
    {
        return "(" + this.x + "|" + this.y + ")";
    }
}
