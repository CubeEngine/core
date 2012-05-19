package de.cubeisland.cubeengine.core.util.math;

/**
 * This class represents a 3D vector
 *
 * @author Phillip Schichtel
 */
public class Vector3
{
    public final double x;
    public final double y;
    public final double z;

    public Vector3(final int x, final int y, final int z)
    {
        this((double)x, (double)y, (double)z);
    }

    public Vector3(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isOrthogonal(Vector3 other)
    {
        return (this.dot(other) == 0.0);
    }

    public boolean isParallel(Vector3 other)
    {
        return (this.x / other.x == this.y / other.y);
    }

    public double dot(Vector3 other)
    {
        return (this.x * other.x + this.y * other.y + this.z * other.z);
    }

    public Vector3 cross(Vector3 other)
    {
        return new Vector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    public Vector3 add(Vector3 other)
    {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 substract(Vector3 other)
    {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3 multiply(int n)
    {
        return this.multiply((double)n);
    }

    public Vector3 multiply(double n)
    {
        return new Vector3(this.x * n, this.y * n, this.z * n);
    }

    public Vector3 divide(int n)
    {
        return this.multiply((double)n);
    }

    public Vector3 divide(double n)
    {
        return new Vector3(this.x / n, this.y / n, this.z / n);
    }

    public double squaredLength()
    {
        return (Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
    }

    public double length()
    {
        return Math.sqrt(this.squaredLength());
    }

    public Vector3 distanceVector(Vector3 other)
    {
        return other.substract(this);
    }

    public double squaredDistance(Vector3 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    public double distance(Vector3 other)
    {
        return this.distanceVector(other).length();
    }

    public double crossAngle(Vector3 other)
    {
        return this.crossAngle(other, true);
    }

    public double crossAngle(Vector3 other, boolean degree)
    {
        double result = Math.acos(this.dot(other) / (this.length() * other.length()));
        if (degree)
        {
            result *= 180 / Math.PI;
        }
        return result;
    }

    public Vector3 normalize()
    {
        return this.divide(this.length());
    }

    public Vector3 midpoint(Vector3 other)
    {
        return this.add(other.substract(this).divide(2));
    }

    public Vector2 project2D()
    {
        return new Vector2(this.x, this.y);
    }

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

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public String toString()
    {
        return "(" + this.x + "|" + this.y  + "|" + this.z + ")";
    }
}
