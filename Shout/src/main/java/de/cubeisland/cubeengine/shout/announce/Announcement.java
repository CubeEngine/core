package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.i18n.ClonedLanguage;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.i18n.NormalLanguage;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to represent an announcement.
 */
public class Announcement
{
    private final String name;
    private final String permNode;
    private final List<String> worlds;
    private final Map<String, String[]> messages;
    private final long delay;

    public Announcement(Announcement acm)
    {
        Validate.notNull(acm);

        this.name = acm.name;
        this.permNode = acm.permNode;
        this.worlds = new ArrayList<String>(acm.getWorlds());
        this.messages = new THashMap<String, String[]>(acm.messages);
        this.delay = acm.delay;
    }

    /**
     * Constructor of announcement
     *
     * @param name          This announcements unique name
     * @param permNode      This announcements permNode
     * @param worlds        This announcements worlds
     * @param messages      This announcements messages
     * @param delay         This announcements delay
     */
    public Announcement(String name, String permNode, List<String> worlds, Map<String, String[]> messages, long delay)
    {
        Validate.notEmpty(name, "The announcement must have a name");
        Validate.notEmpty(permNode, "The announcement must have a permission");
        Validate.notEmpty(worlds, "The announcement must have a world");
        Validate.notEmpty(messages, "The announcement must have one or more messages");
        Validate.isTrue(delay > 0, "The announcement needs a delay");

        this.name = name;
        this.permNode = permNode;
        this.worlds = worlds;
        this.messages = messages;
        this.delay = delay;
    }

    /**
     * Get the message from this announcement in a specified language
     *
     * @param locale	The language to get the message in
     * @return	The message in that language if exist.
     */
    public String[] getMessage(String locale)
    {
        locale = I18n.normalizeLanguage(locale);
        if (this.messages.containsKey(locale))
        {
            return messages.get(locale);
        }

        final I18n i18n = CubeEngine.getI18n();
        Language lang = i18n.getLanguage(locale);
        if (lang != null)
        {
            if (lang instanceof NormalLanguage)
            {
                lang = ((NormalLanguage)lang).getParent();
                if (lang != null)
                {
                    return this.messages.get(lang.getCode());
                }
            }

            if (lang instanceof ClonedLanguage)
            {
                return this.messages.get(((ClonedLanguage)lang).getOriginal().getCode());
            }
        }

        return this.messages.get(i18n.getDefaultLanguage());
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
    public String getFirstWorld()
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
}
