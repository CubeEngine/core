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
package de.cubeisland.cubeengine.core.util;

import java.util.regex.Pattern;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import org.fusesource.jansi.Ansi;

/**
 * This enum contains all of Minecraft's chat format codes and some utility methods to parse them.
 */
public enum ChatFormat
{
    BLACK('0', Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString()),
    DARK_BLUE('1', Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString()),
    DARK_GREEN('2', Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString()),
    DARK_AQUA('3', Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString()),
    DARK_RED('4', Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString()),
    PURPLE('5', Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString()),
    GOLD('6', Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString()),
    GREY('7', Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString()),
    DARK_GREY('8', Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString()),
    INDIGO('9', Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString()),
    BRIGHT_GREEN('a', Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString()),
    AQUA('b', Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString()),
    RED('c', Ansi.ansi().fg(Ansi.Color.RED).bold().toString()),
    PINK('d', Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString()),
    YELLOW('e', Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString()),
    WHITE('f', Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString()),
    MAGIC('k', Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString()),
    BOLD('l', Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString()),
    STRIKE('m', Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString()),
    UNDERLINE('n', Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString()),
    ITALIC('o', Ansi.ansi().a(Ansi.Attribute.ITALIC).toString()),
    RESET('r', Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());

    private static final Pattern PARSE_FOR_CONSOLE = Pattern.compile("");
    private static final char BASE_CHAR = '\u00A7';
    private static final TCharObjectMap<ChatFormat> FORMAT_CHARS_MAP;
    private static final String FORMAT_CHARS_STRING = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final Pattern STRIP_FORMATS = Pattern.compile(BASE_CHAR + "[" + FORMAT_CHARS_STRING + "]");
    private final char formatChar;
    private final String string;
    private final String ansiCode;

    private ChatFormat(char formatChar, String ansiCode)
    {
        this.formatChar = formatChar;
        this.ansiCode = ansiCode;
        this.string = String.valueOf(new char[] {
            BASE_CHAR, formatChar
        });
    }

    /**
     * Gets a chat format by it's char
     *
     * @param theChar the char to look for
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

    public String getAnsiCode()
    {
        return this.ansiCode;
    }

    /**
     * Removes all the format codes from a string
     *
     * @param string the string
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
     * Parses the chat format strings
     *
     * @param string the string to parse
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

    public String toString()
    {
        return this.string;
    }

    static
    {
        ChatFormat[] values = values();
        FORMAT_CHARS_MAP = new TCharObjectHashMap<ChatFormat>(values.length);
        for (ChatFormat format : values)
        {
            FORMAT_CHARS_MAP.put(format.getChar(), format);
        }
    }
}
