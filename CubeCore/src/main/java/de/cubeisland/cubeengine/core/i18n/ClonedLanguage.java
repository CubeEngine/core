package de.cubeisland.cubeengine.core.i18n;

import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Phillip Schichtel
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
    public String getCode()
    {
        return this.code;
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
    public String getTranslation(String cat, String message)
    {
        return this.original.getTranslation(cat, message);
    }

    @Override
    public Map<String, String> getMessages(String cat)
    {
        return this.original.getMessages(cat);
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
        this.original.clean();
    }
}
