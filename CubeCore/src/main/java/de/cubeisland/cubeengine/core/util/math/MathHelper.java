package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author Phillip Schichtel
 */
public class MathHelper
{
    private static final double RADIANS_DEGREE_CONVERTER = 180.0 / Math.PI;

    private MathHelper()
    {}
    
    public static double radiansToDegree(double radians)
    {
        return radians * RADIANS_DEGREE_CONVERTER;
    }

    public static double degreeToRadians(double degree)
    {
        return degree / RADIANS_DEGREE_CONVERTER;
    }

    public static int floor(double num)
    {
        final int floored = (int)num;
        return (floored == num ? floored : floored - (int)(Double.doubleToRawLongBits(num) >>> 63));
    }

    public static int ceil(double num)
    {
        final int floored = (int)num;
        return (floored == num ? floored : floored +(int)(Double.doubleToRawLongBits(num) >>> 63));
    }

    public static int round(double num)
    {
        return floor(num + 0.5);
    }

    public static double pow(double x, int n)
    {
        if (n == 0)
        {
            return 1.0;
        }
        for (int i = 1; i < n; ++i)
        {
            x *= x;
        }
        return x;
    }

    public static double pow(int x, int n)
    {
        double result = 1.0;
        while (n != 0)
        {
            if ((x & 1) == 1)
            {
                result *= x;
                --n;
            }
            x *= x;
            n /= 2;
        }
        return result;
    }
}
