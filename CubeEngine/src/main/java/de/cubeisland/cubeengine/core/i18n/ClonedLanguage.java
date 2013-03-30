package de.cubeisland.cubeengine.core.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * This class represents a clone of another language.
 */
public class ClonedLanguage implements Language
{
    private final Locale locale;
    private final Language original;

    public ClonedLanguage(Locale locale, Language original)
    {
        if (locale == null)
        {
            throw new NullPointerException("The code must not be null!");
        }
        if (original == null)
        {
            throw new NullPointerException("The original must not be null!");
        }

        this.locale = locale;
        this.original = original;
    }

    public static ClonedLanguage clone(Locale locale, Language original)
    {
        try
        {
            return new ClonedLanguage(locale, original);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    @Override
    public String getName()
    {
        return this.original.getName();
    }

    @Override
    public Locale getLocale()
    {
        return this.locale;
    }

    @Override
    public String getLocalName()
    {
        return this.original.getLocalName();
    }

    @Override
    public String getTranslation(String message)
    {
        return this.original.getTranslation(message);
    }

    @Override
    public Map<String, String> getMessages()
    {
        return this.original.getMessages();
    }

    @Override
    public boolean equals(Locale locale)
    {
        return this.locale.equals(locale);
    }

    @Override
    public int hashCode()
    {
        return this.locale.hashCode();
    }

    @Override
    public void clean()
    {
        this.original.clean();
    }

    /**
     * This method returns the language cloned by this language
     *
     * @return the original
     */
    public Language getOriginal()
    {
        return this.original;
    }
}
