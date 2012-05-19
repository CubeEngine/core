package de.cubeisland.cubeengine.core.util.math;

/**
 * This class represents a 2D vector
 *
 * @author Phillip Schichtel
 */
public class Vector2
{
    public final double x;
    public final double y;

    public Vector2(int x, int y)
    {
        this((double)x, (double)y);
    }

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public boolean isOrthogonal(Vector2 other)
    {
        return (this.dot(other) == 0.0);
    }

    public boolean isParallel(Vector2 other)
    {
        return (this.x / other.x == this.y / other.y);
    }

    public double dot(Vector2 other)
    {
        return (this.x * other.x + this.y * other.y);
    }

    public Vector2 add(Vector2 other)
    {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 substract(Vector2 other)
    {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(int n)
    {
        return this.multiply((double)n);
    }

    public Vector2 multiply(double n)
    {
        return new Vector2(this.x * n, this.y * n);
    }

    public Vector2 divide(int n)
    {
        return this.multiply((double)n);
    }

    public Vector2 divide(double n)
    {
        return new Vector2(this.x / n, this.y / n);
    }

    public double squaredLength()
    {
        return (Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public double length()
    {
        return Math.sqrt(this.squaredLength());
    }

    public Vector2 distanceVector(Vector2 other)
    {
        return other.substract(this);
    }

    public double squaredDistance(Vector2 other)
    {
        return this.distanceVector(other).squaredLength();
    }

    public double distance(Vector2 other)
    {
        return this.distanceVector(other).length();
    }

    public double crossAngle(Vector2 other)
    {
        return this.crossAngle(other, true);
    }

    public double crossAngle(Vector2 other, boolean degree)
    {
        double result = Math.acos(this.dot(other) / (this.length() * other.length()));
        if (degree)
        {
            result *= 180 / Math.PI;
        }
        return result;
    }

    public Vector2 normalize()
    {
        return this.divide(this.length());
    }

    public Vector2 midpoint(Vector2 other)
    {
        return this.add(other.substract(this).divide(2));
    }

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

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public String toString()
    {
        return "(" + this.x + "|" + this.y + ")";
    }
}
