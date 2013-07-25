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
 * This class represents a 2D vector with integer coordinates.
 */
public class BlockVector2
{
    public final int x;
    public final int z;

    /**
     * Creates a new BlockVector2 with a pair of int
     *
     * @param x the x value
     * @param z the z value
     */
    public BlockVector2(final int x, final int z)
    {
        this.x = x;
        this.z = z;
    }

    /**
     * Checks whether an other BlockVector ist orthogonal to this one
     *
     * @param other the other BlockVector
     * @return whether the other BlockVector is orthogonal to this one
     */
    public boolean isOrthogonal(BlockVector2 other)
    {
        return (this.dot(other) == 0.0);
    }

    /**
     * Checks whether an other BlockVector ist parallel to this one
     *
     * @param other the other BlockVector
     * @return whether the other BlockVector is parallel to this one
     */
    public boolean isParallel(BlockVector2 other)
    {
        return (this.x / other.x == this.z / other.z);
    }

    /**
     * Returns the scalar product of this Vektor and the other
     *
     * @param other the second vector to multiply with
     * @return the scalar product
     */
    public double dot(BlockVector2 other)
    {
        return (this.x * other.x + this.z * other.z);
    }

    /**
     * Returns the sum of an other BlockVector and this one as a BlockVector
     *
     * @param other the BlockVector to add
     * @return the new Vector
     */
    public BlockVector2 add(BlockVector2 other)
    {
        return new BlockVector2(this.x + other.x, this.z + other.z);
    }

    /**
     * Returns the difference between an other BlockVector and this one as a
     * BlockVector
     *
     * @param other the BlockVector to subtract
     * @return the new Vector
     */
    public BlockVector2 subtract(BlockVector2 other)
    {
        return new BlockVector2(this.x - other.x, this.z - other.z);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public BlockVector2 multiply(int n)
    {
        return new BlockVector2(this.x * n, this.z * n);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param n the factor to multiply with
     * @return the new Vector
     */
    public BlockVector2 multiply(double n)
    {
        return new BlockVector2((int)(this.x * n), (int)(this.z * n));
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public BlockVector2 divide(int n)
    {
        return this.divide((double)n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param n the quotient to divide with
     * @return the new Vector
     */
    public BlockVector2 divide(double n)
    {
        return new BlockVector2((int)(this.x / n), (int)(this.z / n));
    }

    /**
     * Returns the squared length
     *
     * @return the length²
     */
    public double squaredLength()
    {
        return (Math.pow(this.x, 2) + Math.pow(this.z, 2));
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
     * Returns the distance between an other BlockVector and this one as a
     * BlockVector
     *
     * @param other the BlockVector to subtract from
     * @return the new Vector
     */
    public BlockVector2 distanceVector(BlockVector2 other)
    {
        return other.subtract(this);
    }

    /**
     * Returns the squared distance between an other BlockVector and this one
     *
     * @param other the BlockVector to get the distance from
     * @return the squared distance
     */
    public double squaredDistance(BlockVector2 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    /**
     * Returns the distance between an other BlockVector and this one
     *
     * @param other the BlockVector to get the distance from
     * @return the distance
     */
    public double distance(BlockVector2 other)
    {
        return this.distanceVector(other).length();
    }

    /**
     * Returns the angle of the other Vector to this one
     *
     * @param other the other BlockVector to calculate the angle to
     * @return the angle between the vectors
     */
    public double crossAngle(BlockVector2 other)
    {
        return Math.acos(this.dot(other) / (this.length() * other.length()));
    }

    /**
     * Returns the normalized Vector
     *
     * @return the normalized BlockVector
     */
    public BlockVector2 normalize()
    {
        return this.divide(this.length());
    }

    /**
     * Returns a BlockVector to the midpoint between this Vector and the other
     *
     * @param other the other BlockVector
     * @return the midpoint
     */
    public BlockVector2 midpoint(BlockVector2 other)
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
        if (!(o instanceof BlockVector2))
        {
            return false;
        }

        BlockVector2 other = (BlockVector2)o;

        return (this.x == other.x && this.z == other.z);
    }

    /**
     * Returns a HashCode representing this Vector
     *
     * @return the HashCode of this BlockVector
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    /**
     * Returns a String representing this Vector
     *
     * @return the String representing this BlockVector
     */
    @Override
    public String toString()
    {
        return "(" + this.x + "|" + this.z + ")";
    }
}
