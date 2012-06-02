package de.cubeisland.cubeengine.core.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Phillip Schichtel
 */
public class Language
{
    private final String code;
    private final String name;
    private final String localName;
    private final Set<String> countries;
    private final Map<String, Map<String, String>> messages;
    private final File messageDir;
    private final JsonParser parser;

    public Language(String code, File languageDir) throws FileNotFoundException, IllegalStateException
    {
        this.code = code;
        this.messageDir = new File(languageDir, code);
        this.countries = new THashSet<String>();
        this.messages = new THashMap<String, Map<String, String>>();
        this.parser = new JsonParser();

        JsonObject root;
        root = parser.parse(new FileReader(new File(languageDir, code + ".json"))).getAsJsonObject();

        this.name = root.get("name").getAsString();
        this.localName = root.get("localName").getAsString();

        if (!this.code.equalsIgnoreCase(I18n.SOURCE_LANGUAGE))
        {
            for (JsonElement elem : root.getAsJsonArray("countries"))
            {
                if (elem.isJsonPrimitive())
                {
                    this.countries.add(elem.getAsString());
                }
            }
        }
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

    public Set<String> getCountries()
    {
        return this.countries;
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
            JsonElement root = parser.parse(new FileReader(new File(this.messageDir, cat + ".json")));
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
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
        return null;
    }
}
