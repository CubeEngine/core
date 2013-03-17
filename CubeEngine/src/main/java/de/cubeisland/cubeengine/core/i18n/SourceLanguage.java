package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import gnu.trove.map.hash.THashMap;

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
    private final Map<String, String> messages = new THashMap<String, String>();

    SourceLanguage()
    {}

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
    public String getTranslation(String message)
    {
        String translation = this.messages.get(message);
        if (translation == null)
        {
            this.messages.put(message, translation = ChatFormat.parseFormats(message));
        }

        return translation;
    }

    @Override
    public Map<String, String> getMessages()
    {
        return new THashMap<String, String>(this.messages);
    }

    @Override
    public boolean equals(Locale locale)
    {
        return this.locale.equals(locale);
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
