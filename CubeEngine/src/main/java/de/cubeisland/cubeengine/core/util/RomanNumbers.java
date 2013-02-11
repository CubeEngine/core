package de.cubeisland.cubeengine.core.util;

public class RomanNumbers {
    private static final String[] ROMAN = {"M", "CM", "D", "CD", "C", "XC", "L",
            "XL", "X", "IX", "V", "IV", "I"};
    private static final int[] INTVAL = {1000, 900, 500, 400,  100,   90,  50,
            40,   10,    9,   5,   4,    1};

    public static String intToRoman(int intValue) {
        if (intValue <= 0 || intValue >= 4000) {
            throw new NumberFormatException(intValue+ " cannot be transformed into Roman");
        }
        String roman = "";
        for (int i = 0; i < ROMAN.length; i++)
        {
            while (intValue >= INTVAL[i])
            {
                intValue -= INTVAL[i];
                roman  += ROMAN[i];
            }
        }
        return roman;
    }
}