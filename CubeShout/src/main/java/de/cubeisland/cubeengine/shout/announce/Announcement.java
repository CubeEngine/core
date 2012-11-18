package de.cubeisland.cubeengine.shout.announce;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.java.swing.plaf.motif.MotifDesktopIconUI;
import org.apache.commons.lang.Validate;

/**
 * Class to represent an announcement.
 */
public class Announcement
{
    private final String name;
    private final String defaultLocale;
    private final String permNode;
    private final List<String> worlds;
    private final Map<String, String> messages;
    private final long delay;
    private final boolean motd;

    /**
     * Constructor of announcement
     *
     * @param name          This announcements unique name
     * @param defaultLocale This announcements default locale
     * @param permNode      This announcements permNode
     * @param worlds        This announcements worlds
     * @param messages      This announcements messages
     * @param delay         This announcements delay
     */
    public Announcement(String name, String defaultLocale, String permNode, List<String> worlds, Map<String, String> messages, long delay, boolean motd)
    {
        this.name = name;
        this.defaultLocale = defaultLocale;
        this.permNode = permNode;
        this.worlds = worlds;
        this.messages = messages;
        this.delay = delay;
        this.motd = motd;
    }

    /**
     * Constructor of announcement
     *
     * @param name          This announcements unique name
     * @param defaultLocale This announcements default locale
     * @param permNode      This announcements permNode
     * @param world        This announcements world
     * @param messages      This announcements messages
     * @param delay         This announcements delay
     */
    public Announcement(String name, String defaultLocale, String permNode, String world, Map<String, String> messages, long delay, boolean motd)
    {
        this(name, defaultLocale, permNode, Arrays.asList(world), messages, delay, motd);
    }

    /**
     * Get the message from this announcement in the default language, as
     * specified by CubeEngine
     *
     * @return The message for this announcement in default language
     */
    public String getMessage()
    {
        return this.getMessage(this.defaultLocale);
    }

    /**
     * Get the message from this announcement in a specified language
     *
     * @param locale	The language to get the message in
     * @return	The message in that language if exist.
     */
    public String getMessage(String locale)
    {
        return this.messages.containsKey(locale) ? this.messages.get(locale) : this.getMessage();
    }

    /**
     * Get the delay after this message
     *
     * @return The delay in milliseconds
     */
    public long getDelay()
    {
        return this.delay;
    }

    /**
     * Get the permission node for this announcement
     *
     * @return	the permission node for this announcement
     */
    public String getPermNode()
    {
        return this.permNode;
    }

    /**
     * Get the worlds this announcement should be displayed in
     *
     * @return	The worlds this announcement should be displayed in.
     */
    public List<String> getWorlds()
    {
        return this.worlds;
    }

    /**
     * Get this announcements first world
     *
     * @return the first world
     */
    public String getWorld()
    {
        return worlds.get(0);
    }

    public boolean hasWorld(String world)
    {
        return (this.worlds.get(0).equals("*") || this.worlds.contains(world));
    }

    public String getName()
    {
        return this.name;
    }

    public static void validate(String name, String defaultLocale, String permNode, List<String> worlds, Map<String, String> messages, long delay) throws IllegalArgumentException
    {
        Validate.notEmpty(name, "The announcement most have a name");
        Validate.notEmpty(defaultLocale, "The announcement most have a default locale");
        Validate.notEmpty(permNode, "The announcement most have a permission");
        Validate.notEmpty(worlds, "The announcement most have a world");
        Validate.notEmpty(messages, "The announcement most have one or more messages");
        Validate.notNull(delay, "The announcement most have a delay");
        if (delay == 0)
        {
            throw new IllegalArgumentException("The announcement modt have a delay");
        }
    }

    public static void validate(String name, String defaultLocale, String permNode, String world, Map<String, String> messages, long delay) throws IllegalArgumentException
    {
        Announcement.validate(name, defaultLocale, permNode, Arrays.asList(world), messages, delay);
    }

    public boolean isMOTD()
    {
        return motd;
    }
}
