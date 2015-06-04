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
package de.cubeisland.engine.module.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * This enum contains all of Minecraft's chat format codes and some utility methods to parse them.
 */
public enum ChatFormat
{
    BLACK('0', TextColors.BLACK),
    DARK_BLUE('1', TextColors.DARK_BLUE),
    DARK_GREEN('2', TextColors.DARK_GREEN),
    DARK_AQUA('3', TextColors.DARK_AQUA),
    DARK_RED('4', TextColors.DARK_RED),
    PURPLE('5', TextColors.DARK_PURPLE),
    GOLD('6', TextColors.GOLD),
    GREY('7', TextColors.GRAY),
    DARK_GREY('8', TextColors.DARK_GRAY),
    INDIGO('9', TextColors.BLUE),
    BRIGHT_GREEN('a', TextColors.GREEN),
    AQUA('b', TextColors.AQUA),
    RED('c', TextColors.RED),
    PINK('d', TextColors.LIGHT_PURPLE),
    YELLOW('e', TextColors.YELLOW),
    WHITE('f', TextColors.WHITE),
    MAGIC('k', TextStyles.OBFUSCATED),
    BOLD('l', TextStyles.BOLD),
    STRIKE('m', TextStyles.STRIKETHROUGH),
    UNDERLINE('n', TextStyles.UNDERLINE),
    ITALIC('o', TextStyles.ITALIC),
    RESET('r', TextColors.RESET);

    private static final Pattern PARSE_FOR_CONSOLE = Pattern.compile("");
    public static final char BASE_CHAR = '\u00A7';
    private static final Map<Character, ChatFormat> FORMAT_CHARS_MAP;
    private static final String FORMAT_CHARS_STRING = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final Pattern STRIP_FORMATS = Pattern.compile(BASE_CHAR + "[" + FORMAT_CHARS_STRING + "]");
    private static final Pattern STRIP_REDUNDANT_FORMATS = Pattern.compile("(?:[&§][0-9a-fk-r])+([&§][0-9a-fk-r])");

    private final char formatChar;
    private BaseFormatting base;
    private final String string;

    ChatFormat(char formatChar, BaseFormatting base)
    {
        this.formatChar = formatChar;
        this.base = base;
        this.string = String.valueOf(new char[]{
            BASE_CHAR, formatChar
        });
    }

    /**
     * Gets a chat format by it's char
     *
     * @param theChar the char to look for
     *
     * @return the ChatFormat or null if not found
     */
    public static ChatFormat getByChar(char theChar)
    {
        return FORMAT_CHARS_MAP.get(theChar);
    }

    public char getChar()
    {
        return this.formatChar;
    }

    /**
     * Removes all the format codes from a string
     *
     * @param string the string
     *
     * @return the stripped string
     */
    public static String stripFormats(String string)
    {
        if (string == null)
        {
            return null;
        }
        return STRIP_FORMATS.matcher(string).replaceAll("");
    }

    /**
     * Removes all the redundant format codes from a string
     *
     * @param string the string
     *
     * @return the stripped string
     */
    public static String stripRedundantFormats(String string)
    {
        return STRIP_REDUNDANT_FORMATS.matcher(string).replaceAll("$1");
    }

    /**
     * Parses the chat format strings
     *
     * @param string the string to parse
     *
     * @return the parsed string
     */
    public static String parseFormats(String string)
    {
        if (string == null)
        {
            return null;
        }
        return parseFormats('&', string);
    }

    /**
     * Parses the chat format strings
     *
     * @param baseChar the char used to indicate a format code
     * @param string   the string to parse
     *
     * @return the parsed string
     */
    public static String parseFormats(char baseChar, String string)
    {
        if (string == null)
        {
            return null;
        }
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length - 1; i++)
        {
            if ((chars[i] != baseChar) || (FORMAT_CHARS_STRING.indexOf(chars[(i + 1)]) == -1))
            {
                continue;
            }
            chars[i] = BASE_CHAR;
            i++;
        }

        return new String(chars);
    }

    @Override
    public String toString()
    {
        return this.string;
    }

    static
    {
        ChatFormat[] values = values();
        FORMAT_CHARS_MAP = new HashMap<>(values.length);
        for (ChatFormat format : values)
        {
            FORMAT_CHARS_MAP.put(format.getChar(), format);
        }
    }

    public String getName()
    {
        return base.getName();
    }
}