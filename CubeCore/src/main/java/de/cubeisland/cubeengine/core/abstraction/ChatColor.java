package de.cubeisland.cubeengine.core.abstraction;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author CodeInfection
 */
public enum ChatColor
{
    BLACK('0', 0x0),
    DARK_BLUE('1', 0x1),
    DARK_GREEN('2', 0x2),
    DARK_CYAN('3', 0x3),
    DARK_RED('4', 0x4),
    PURPLE('5', 0x5),
    GOLD('6', 0x6),
    GRAY('7', 0x7),
    DARK_GRAY('8', 0x8),
    BLUE('9', 0x9),
    BRIGHT_GREEN('a', 0xA),
    CYAN('b', 0xB),
    RED('c', 0xC),
    PINK('d', 0xD),
    YELLOW('e', 0xE),
    WHITE('f', 0xF),
    MAGIC('k', 0x10);
    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile(String.valueOf(COLOR_CHAR) + "[0-9A-FK]", Pattern.CASE_INSENSITIVE);
    private final static Map<Byte, ChatColor> BY_CODE = new HashMap<Byte, ChatColor>(values().length);
    private final static Map<Character, ChatColor> BY_CHAR = new HashMap<Character, ChatColor>(values().length);
    private final char character;
    private final byte code;
    private final String string;

    private ChatColor(char character, int code)
    {
        this.character = character;
        this.code = (byte)code;
        this.string = new String(new char[]
            {
                COLOR_CHAR, character
            });
    }

    public char getCharacter()
    {
        return this.character;
    }

    public byte getCode()
    {
        return this.code;
    }

    public static ChatColor getByCharacter(char character)
    {
        return BY_CHAR.get(character);
    }

    public static ChatColor getByCode(int code)
    {
        return getByCode((byte)code);
    }

    public static ChatColor getByCode(byte code)
    {
        return BY_CODE.get(code);
    }

    public static String stripColors(String string)
    {
        if (string == null)
        {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    static
    {
        for (ChatColor chatColor : values())
        {
            BY_CHAR.put(chatColor.getCharacter(), chatColor);
            BY_CODE.put(chatColor.getCode(), chatColor);
        }
    }
}
