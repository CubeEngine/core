package de.cubeisland.cubeengine.core.i18n;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Phillip Schichtel
 */
public class Language
{
    private final String name;
    private final Set<String> countries;
    private final Map<String, Map<String, String>> messages;

    public Language(String name)
    {
        this.name = name;
        this.countries = new THashSet<String>();
        this.messages = new THashMap<String, Map<String, String>>();
    }

    public String getName()
    {
        return this.name;
    }

    public void addMessages(String cat, Map<String, String> messages)
    {
        if (cat == null)
        {
            throw new IllegalArgumentException("The category must not be null!");
        }
        if (messages == null)
        {
            throw new IllegalArgumentException("The messages must not be null!");
        }
        this.messages.put(cat, messages);
    }

    public String getTranslation(String cat, String message)
    {
        Map<String, String> catMessages = this.messages.get(cat);
        if (catMessages != null)
        {
            return catMessages.get(message);
        }
        return null;
    }
}
