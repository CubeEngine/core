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
 * This class represents a 3D vector.
 */
public class Vector3
{
    public final double x;
    public final double y;
    public final double z;

    /**
     * Creates a new Vector3 with a triple of int
     *
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public Vector3(final int x, final int y, final int z)
    {
        this((double)x, (double)y, (double)z);
    }

    /**
     * Creates a new Vector3 with a triple of double
     *
     * @param x the x value
     * @param y the y value
     * @param z the z value
     */
    public Vector3(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Checks whether an other Vector3 ist orthogonal to this one
     *
     * @param other the other Vector3
     * @return whether the other Vector3 is orthogonal to this one
     */
    public boolean isOrthogonal(Vector3 other)
    {
        return (this.dot(other) == 0.0);
    }

    /**
     * Checks whether an other Vector3 ist parallel to this one
     *
     * @param other the other Vector3
     * @return whether the other Vector3 is parallel to this one
     */
    public boolean isParallel(Vector3 other)
    {
        return (this.x / other.x == this.y / other.y);
    }

    /**
     * Returns the scalar product of this vector and the other
     *
     * @param other the second vector to multiply with
     * @return the scalar product
     */
    public double dot(Vector3 other)
    {
        return (this.x * other.x + this.y * other.y + this.z * other.z);
    }

    /**
     * Returns the cross product of this vector and the other
     *
     * @param other the other vector
     * @return the cross product
     */
    public Vector3 cross(Vector3 other)
    {
        return new Vector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x);
    }

    /**
     * Returns the sum of an other Vector3 and this one as a Vector3
     *
     * @param other the Vector3 to add
     * @return the new Vector
     */
    public Vector3 add(Vector3 other)
    {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Returns the difference between an other Vector3 and this one as a Vector3
     *
     * @param other the Vector3 to subtract
     * @return the new Vector
     */
    public Vector3 subtract(Vector3 other)
    {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public Vector3 multiply(int n)
    {
        return this.multiply((double)n);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public Vector3 multiply(double n)
    {
        return new Vector3(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public Vector3 divide(int n)
    {
        return this.divide((double)n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public Vector3 divide(double n)
    {
        return new Vector3(this.x / n, this.y / n, this.z / n);
    }

    /**
     * Returns the squared length
     *
     * @return the length²
     */
    public double squaredLength()
    {
        return (Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
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
     * Returns the distance between an other Vector3 and this one as a Vector3
     *
     * @param other the Vector3 to subtract from
     * @return the new Vector
     */
    public Vector3 distanceVector(Vector3 other)
    {
        return other.subtract(this);
    }

    /**
     * Returns the squared distance between an other Vector3 and this one
     *
     * @param other the Vector3 to get the distance from
     * @return the squared distance
     */
    public double squaredDistance(Vector3 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    /**
     * Returns the distance between an other Vector3 and this one
     *
     * @param other the Vector3 to get the distance from
     * @return the distance
     */
    public double distance(Vector3 other)
    {
        return this.distanceVector(other).length();
    }

    /**
     * Returns the angle of the other Vector to this one
     *
     * @param other the other Vector3 to calculate the angle to
     * @return the angle between the vectors
     */
    public double crossAngle(Vector3 other)
    {
        return Math.acos(this.dot(other) / (this.length() * other.length()));
    }

    /**
     * Returns the normalized Vector
     *
     * @return the normalized Vector3
     */
    public Vector3 normalize()
    {
        return this.divide(this.length());
    }

    /**
     * Returns a Vector3 to the midpoint between this Vector and the other
     *
     * @param other the other Vector3
     * @return the midpoint
     */
    public Vector3 midpoint(Vector3 other)
    {
        return this.add(other.subtract(this).divide(2));
    }

    /**
     * Projection into 2D Vector
     *
     * @return the projection
     */
    public Vector2 project2D()
    {
        return new Vector2(this.x, this.y);
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
        if (!(o instanceof Vector3))
        {
            return false;
        }

        Vector3 other = (Vector3)o;

        return (this.x == other.x && this.y == other.y && this.z == other.z);
    }

    /**
     * Returns a HashCode representing this Vector
     *
     * @return the HashCode of this Vector3
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    /**
     * Returns a String representing this Vector
     *
     * @return the String representing this Vector3
     */
    @Override
    public String toString()
    {
        return "(" + this.x + "|" + this.y + "|" + this.z + ")";
    }
}
