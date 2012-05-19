package de.cubeisland.cubeengine.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
    MAGIC('k', 0x10),
    BOLD('l', 0x11),
    STRIKETHROUGH('m', 0x12),
    UNDERLINE('n', 0x13),
    ITALIC('o', 0x14),
    RESET('r', 0x15);
    
    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN;
    private static final String CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final Map<Byte, ChatColor> BY_CODE;
    private static final Map<Character, ChatColor> BY_CHAR;
    private final char character;
    private final byte code;
    private final String string;

    private ChatColor(char character, int code)
    {
        this.character = character;
        this.code = (byte) code;
        this.string = new String(new char[]{COLOR_CHAR, character});
    }

    public char getChar()
    {
        return this.character;
    }

    public byte getCode()
    {
        return this.code;
    }

    public static ChatColor getByCharacter(char character)
    {
        return BY_CHAR.get(Character.valueOf(character));
    }

    public static ChatColor getByCode(int code)
    {
        return getByCode((byte)code);
    }

    public static ChatColor getByCode(byte code)
    {
        return BY_CODE.get(Byte.valueOf(code));
    }

    public static String stripColors(String string)
    {
        if (string == null)
        {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public static String translateAlternateColorCodes(char colorChar, String text)
    {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++)
        {
            if ((chars[i] != colorChar) || (CHARS.indexOf(chars[(i + 1)]) <= -1))
            {
                continue;
            }
            chars[i] = COLOR_CHAR;
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
        STRIP_COLOR_PATTERN = Pattern.compile(COLOR_CHAR + "[" + CHARS + "]");
        BY_CODE = new HashMap(values().length);
        BY_CHAR = new HashMap(values().length);

        for (ChatColor chatColor : values())
        {
            BY_CHAR.put(Character.valueOf(chatColor.getChar()), chatColor);
            BY_CODE.put(Byte.valueOf(chatColor.getCode()), chatColor);
        }
    }
}