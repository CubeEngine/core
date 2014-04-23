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
 * This class provides some utility methods for maths.
 */
public class MathHelper
{
    private static final double RADIANS_DEGREE_CONVERTER = 180.0 / Math.PI;

    private MathHelper()
    {}

    /**
     * This method converts radians to degrees
     *
     * @param radians the radian value
     * @return the degree value
     */
    public static double radiansToDegree(double radians)
    {
        return radians * RADIANS_DEGREE_CONVERTER;
    }

    /**
     * This method converts degrees to radians
     *
     * @param degrees the degree value
     * @return the radian value
     */
    public static double degreeToRadians(double degrees)
    {
        return degrees / RADIANS_DEGREE_CONVERTER;
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
     * @return the rounded up number
     */
    public static int ceil(double num)
    {
        final int floored = (int)num;
        return (floored == num ? floored : floored + (int)(Double.doubleToRawLongBits(num) >>> 63));
    }

    /**
     * This is a fast round implementation based in the fast floor implementation
     *
     * @param num the number to floor
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
     * @return the n-th power of x
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
     * @return the n-th power of x
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
