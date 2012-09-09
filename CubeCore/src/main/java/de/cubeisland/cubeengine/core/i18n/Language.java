package de.cubeisland.cubeengine.core.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public class Language
{
    private final String code;
    private final String name;
    private final String localName;
    private final Map<String, Map<String, String>> messages;
    private final File messageDir;
    private final JsonParser parser;

    public Language(LanguageConfiguration config, File languageDir)
    {
        Validate.notNull(config.code, "The code must not be null!");
        Validate.notNull(config.name, "The name must not be null!");
        Validate.notNull(config.localName, "The local name must not be null!");
        
        this.code = I18n.normalizeLanguage(config.code);
        this.name = config.name;
        this.localName = config.localName;
        this.messageDir = new File(languageDir, this.code);
        this.messages = new ConcurrentHashMap<String, Map<String, String>>();
        this.parser = new JsonParser();
    }

    public String getCode()
    {
        return this.code;
    }

    public String getName()
    {
        return this.name;
    }

    public String getLocalName()
    {
        return this.localName;
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
        if (catMessages == null)
        {
            catMessages = this.loadMessages(cat);
        }
        if (catMessages != null)
        {
            return catMessages.get(message);
        }
        return null;
    }

    private Map<String, String> loadMessages(String cat)
    {
        try
        {
            FileReader reader = new FileReader(new File(this.messageDir, cat + ".json"));
            JsonElement root = parser.parse(reader);
            reader.close();
            if (root.isJsonObject())
            {
                Map<String, String> catMessages = new THashMap<String, String>();
                JsonElement elem;
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet())
                {
                    elem = entry.getValue();
                    if (elem.isJsonPrimitive())
                    {
                        catMessages.put(entry.getKey(), elem.getAsString());
                    }
                }
                if (!catMessages.isEmpty())
                {
                    this.messages.put(cat, catMessages);
                    return catMessages;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        return null;
    }

    void clean()
    {
        this.messages.clear();
    }
}