package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.util.Cleanable;
import java.util.Locale;
import java.util.Map;

/**
 * This interface represents a language containing translations.
 */
public interface Language extends Cleanable
{

    /**
     * Returns the language's locale
     *
     * @return a locale
     */
    public Locale getLocale();

    /**
     * Return the language's name
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the language's local name
     *
     * @return the local name
     */
    public String getLocalName();

    /**
     * Gets a translation from this language
     *
     * @param message the message
     * @return the translation
     */
    public String getTranslation(String message);

    /**
     * Returns a map of all translations of the given category
     *
     * @return all translations of the category
     */
    public Map<String, String> getMessages();

    public boolean equals(Locale locale);
}
