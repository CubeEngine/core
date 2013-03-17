package de.cubeisland.cubeengine.core.i18n;

import org.apache.commons.lang.Validate;

import java.util.Locale;
import java.util.Map;

/**
 * This class represents a clone of another language.
 */
public class ClonedLanguage implements Language
{
    private final String code;
    private final Locale locale;
    private final Language original;

    public ClonedLanguage(String code, Language original)
    {
        code = I18n.normalizeLanguage(code);
        Validate.notNull(code, "The code must not be null!");
        Validate.notNull(original, "The original must not be null!");

        this.code = code;
        this.locale = new Locale(code.substring(0, 2), code.substring(3, 5));
        this.original = original;
    }

    public static ClonedLanguage clone(Language original, String code)
    {
        try
        {
            return new ClonedLanguage(code, original);
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
        return this.code.hashCode();
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
