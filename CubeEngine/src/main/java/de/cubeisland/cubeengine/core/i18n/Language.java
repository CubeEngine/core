package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.util.Cleanable;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public interface Language extends Cleanable
{
    /**
     * Returns the language's locale string
     *
     * @return a locale string
     */
    public String getCode();

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
     * @param cat the category to load the message from
     * @param message the message
     * @return the translation
     */
    public String getTranslation(String cat, String message);

    /**
     * Returns a map of all translations of the given category
     *
     * @param cat the category to return
     * @return all translations of the category
     */
    public Map<String, String> getMessages(String cat);

    public boolean equals(String code);
}
