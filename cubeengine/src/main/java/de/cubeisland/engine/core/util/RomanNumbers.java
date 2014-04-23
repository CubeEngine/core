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
package de.cubeisland.engine.core.util;

public class RomanNumbers
{
    private static final String[] ROMAN = {
    "M", "CM", "D", "CD", "C", "XC", "L",
            "XL", "X", "IX", "V", "IV", "I"
    };
    private static final int[] INTVAL = {
    1000, 900, 500, 400, 100, 90, 50,
            40, 10, 9, 5, 4, 1
    };

    public static String intToRoman(int intValue)
    {
        if (intValue <= 0 || intValue >= 4000)
        {
            throw new NumberFormatException(intValue + " cannot be transformed into Roman");
        }
        String roman = "";
        for (int i = 0; i < ROMAN.length; i++)
        {
            while (intValue >= INTVAL[i])
            {
                intValue -= INTVAL[i];
                roman += ROMAN[i];
            }
        }
        return roman;
    }
}
