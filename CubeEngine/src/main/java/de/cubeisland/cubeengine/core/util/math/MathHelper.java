package de.cubeisland.cubeengine.core.util.math;

/**
 * This class provides some utility methods for maths
 */
public class MathHelper
{
    private static final double RADIANS_DEGREE_CONVERTER = 180.0 / Math.PI;

    private MathHelper()
    {
    }

    /**
     * This method converts radians to degrees
     *
     * @param radians
     * @return 
     */
    public static double radiansToDegree(double radians)
    {
        return radians * RADIANS_DEGREE_CONVERTER;
    }

    /**
     * This method converts degrees to radians
     *
     * @param degree
     * @return 
     */
    public static double degreeToRadians(double degree)
    {
        return degree / RADIANS_DEGREE_CONVERTER;
    }

    /**
     * This is a fast floor implementation
     *
     * @param num the number to floor
     * @return the floored number
     */
    public static int floor(double num)
    {
        final int floored = (int)num;
        return (floored == num ? floored : floored - (int)(Double.doubleToRawLongBits(num) >>> 63));
    }

    /**
     * This is a fast ceil implementation
     *
     * @param num the number to ceil
     * @return the ceiled number
     */
    public static int ceil(double num)
    {
        final int floored = (int)num;
        return (floored == num ? floored : floored + (int)(Double.doubleToRawLongBits(num) >>> 63));
    }

    /**
     * This is a fast round implemantation based in the fast floor implementation
     *
     * @param num the number to floot
     * @return the rounded number
     */
    public static int round(double num)
    {
        return floor(num + 0.5);
    }

    /**
     * This is a fast pow implementation
     *
     * @param x the base
     * @param n the exponent
     * @return
     */
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

    /**
     * This is a fast pow implementation for int values
     *
     * @param x the base
     * @param n the exponent
     * @return 
     */
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
