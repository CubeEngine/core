package de.cubeisland.cubeengine.core.util.math;

/**
 *
 * @author CodeInfection
 */
public class MathHelper
{
    private static final double RADIANS_DEGREE_CONVERTER = 180.0 / Math.PI;

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
}
