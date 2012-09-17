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
    public String getCode();
    public Locale getLocale();
    public String getName();
    public String getLocalName();
    public String getTranslation(String cat, String message);
    public Map<String, String> getMessages(String cat);
    public boolean equals(String code);
}
