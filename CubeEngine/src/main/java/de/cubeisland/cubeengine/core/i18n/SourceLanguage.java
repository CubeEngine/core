package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import gnu.trove.map.hash.THashMap;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents the source language.
 */
public final class SourceLanguage implements Language
{
    private final String code = "en_US";
    private final Locale locale = Locale.US;
    private final String name = "English";
    private final String localName = "English";
    private final Map<String, Map<String, String>> messages = new THashMap<String, Map<String, String>>();

    SourceLanguage()
    {}

    @Override
    public String getCode()
    {
        return this.code;
    }

    @Override
    public Locale getLocale()
    {
        return this.locale;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getLocalName()
    {
        return this.localName;
    }

    @Override
    public String getTranslation(String cat, String message)
    {
        Map<String, String> catMessages = this.messages.get(cat);
        if (catMessages == null)
        {
            this.messages.put(cat, catMessages = new THashMap<String, String>());
        }

        String translation = catMessages.get(message);
        if (translation == null)
        {
            catMessages.put(message, translation = ChatFormat.parseFormats(message));
        }

        return translation;
    }

    @Override
    public Map<String, String> getMessages(String cat)
    {
        if (this.messages.containsKey(cat))
        {
            return Collections.unmodifiableMap(this.messages.get(cat));
        }
        return null;
    }

    @Override
    public boolean equals(String code)
    {
        return this.code.equalsIgnoreCase(code);
    }

    @Override
    public int hashCode()
    {
        return this.code.hashCode();
    }

    @Override
    public void clean()
    {
        this.messages.clear();
    }
}
