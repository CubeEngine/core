package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author Anselm Brehme
 */
public class BlockVector3
{
    public final int x;
    public final int y;
    public final int z;

    /**
     * Creates a new Vektor3 with a triple of int
     *
     * @param x
     * @param y
     * @param z
     */
    public BlockVector3(final int x, final int y, final int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Checks whether an other BlockVector ist orthogonal to this one
     *
     * @param other the other BlockVector
     * @return whether the other BlockVector is orthogonal to this one
     */
    public boolean isOrthogonal(BlockVector3 other)
    {
        return (this.dot(other) == 0.0);
    }

    /**
     * Checks whether an other BlockVector ist parallel to this one
     *
     * @param other the other BlockVector
     * @return whether the other BlockVector is parallel to this one
     */
    public boolean isParallel(BlockVector3 other)
    {
        return (this.x / other.x == this.y / other.y);
    }

    /**
     * Returns the scalar product of this Vektor and the other
     *
     * @param other the second vector to multiply with
     * @return the scalar product
     */
    public double dot(BlockVector3 other)
    {
        return (this.x * other.x + this.y * other.y + this.z * other.z);
    }

    /**
     * Returns the cross product of this Vektor and the other
     *
     * @param other
     * @return the cross product
     */
    public BlockVector3 cross(BlockVector3 other)
    {
        return new BlockVector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x);
    }

    /**
     * Returns the sum of an other BlockVector and this one as a BlockVector
     *
     * @param other the BlockVector to add
     * @return the new Vector
     */
    public BlockVector3 add(BlockVector3 other)
    {
        return new BlockVector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Returns the difference between an other BlockVector and this one as a
     * BlockVector
     *
     * @param other the BlockVector to substract
     * @return the new Vector
     */
    public BlockVector3 substract(BlockVector3 other)
    {
        return new BlockVector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param other the factor to multiply with
     * @return the new Vector
     */
    public BlockVector3 multiply(int n)
    {
        return new BlockVector3(this.x * n, this.y * n, this.z * n);
    }

    /**
     * Returns this vector multiplied with the factor n
     *
     * @param other the factor to multiply with
     * @return the new Vector
     */
    public BlockVector3 multiply(double n)
    {
        return new BlockVector3((int)(this.x * n), (int)(this.y * n), (int)(this.z * n));
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param other the quotient to divide with
     * @return the new Vector
     */
    public BlockVector3 divide(int n)
    {
        return this.divide((double)n);
    }

    /**
     * Returns this vector divided by the quotient n
     *
     * @param other the quotient to divide with
     * @return the new Vector
     */
    public BlockVector3 divide(double n)
    {
        return new BlockVector3((int)(this.x / n), (int)(this.y / n), (int)(this.z / n));
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
     * Returns the distance between an other BlockVector and this one as a
     * BlockVector
     *
     * @param other the BlockVector to substract from
     * @return the new Vector
     */
    public BlockVector3 distanceVector(BlockVector3 other)
    {
        return other.substract(this);
    }

    /**
     * Returns the squared distance between an other BlockVector and this one
     *
     * @param other the BlockVector to get the distance from
     * @return the squared distance
     */
    public double squaredDistance(BlockVector3 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    /**
     * Returns the distance between an other BlockVector and this one
     *
     * @param other the BlockVector to get the distance from
     * @return the distance
     */
    public double distance(BlockVector3 other)
    {
        return this.distanceVector(other).length();
    }

    /**
     * Returns the angle of the other Vector to this one
     *
     * @param other the other BlockVector to calculate the angle to
     * @return the angle between the vectors
     */
    public double crossAngle(BlockVector3 other)
    {
        return Math.acos(this.dot(other) / (this.length() * other.length()));
    }

    /**
     * Returns the normalized Vector
     *
     * @return the normalized BlockVector
     */
    public BlockVector3 normalize()
    {
        return this.divide(this.length());
    }

    /**
     * Returns a BlockVector to the midpoint between this Vector and the other
     *
     * @param other the other BlockVector
     * @return the midpoint
     */
    public BlockVector3 midpoint(BlockVector3 other)
    {
        return this.add(other.substract(this).divide(2));
    }

    /**
     * Projection into horizontal 2D Vector
     *
     * @return the projection
     */
    public BlockVector2 project2D()
    {
        return new BlockVector2(this.x, this.z);
    }

    /**
     * Returns whether the Object o equals this Vector
     *
     * @param other an Object
     * @return whether o is the same Vector as this one
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (!(o instanceof BlockVector3))
        {
            return false;
        }

        BlockVector3 other = (BlockVector3)o;

        return (this.x == other.x && this.y == other.y && this.z == other.z);
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
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
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
        return "(" + this.x + "|" + this.y + "|" + this.z + ")";
    }
}