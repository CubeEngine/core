package de.cubeisland.cubeengine.core.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Validate;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Phillip Schichtel
 */
public class Language implements Cleanable
{
    private final String code;
    private final String name;
    private final String localName;
    private final String parent;
    private Language parentLanguage = null;
    private final Map<String, Map<String, String>> messages;
    private final File messageDir;
    private final JsonParser parser;
    private final Locale locale;
    private static final I18n i18n = CubeEngine.getI18n();

    public Language(LanguageConfiguration config, File languageDir)
    {
        Validate.notNull(config.code, "The code must not be null!");
        Validate.notNull(config.name, "The name must not be null!");
        Validate.notNull(config.localName, "The local name must not be null!");

        this.code = I18n.normalizeLanguage(config.code);
        Validate.notNull(this.code, "The configured language code is invalid!");

        this.name = config.name;
        this.localName = config.localName;
        this.parent = config.parent.toLowerCase(Locale.ENGLISH);
        this.messageDir = new File(languageDir, this.code);
        this.messages = new ConcurrentHashMap<String, Map<String, String>>();
        this.parser = new JsonParser();

        this.locale = new Locale(this.code.substring(0, 2), this.code.substring(3, 5));
    }

    public String getCode()
    {
        return this.code;
    }

    public Locale getLocale()
    {
        return this.locale;
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
            String msg = catMessages.get(message);
            if (msg == null)
            {
                if (parent != null)
                {
                    if (this.parentLanguage == null)
                    {
                        this.parentLanguage = i18n.getLanguage(parent);
                        if (parentLanguage == null)
                        {
                            throw new IllegalStateException("Parent Language " + this.parent + " not found");
                        }
                    }
                    return this.parentLanguage.getTranslation(cat, message);
                }
            }
            return msg;
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
                        catMessages.put(entry.getKey(), ChatFormat.parseFormats(elem.getAsString()));
                    }
                }
                this.updateMessages(cat, catMessages);
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

    private void updateMessages(String cat, Map<String, String> catMessages) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(CubeEngine.getModuleManager().getModule(cat).getResource(CubeEngine.getFileManager().getSourceOf(new File(this.messageDir, cat + ".json"))));
        JsonElement root = parser.parse(reader);
        reader.close();
        if (root.isJsonObject())
        {
            JsonElement elem;
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet())
            {
                if (catMessages.containsKey(entry.getKey()))
                {
                    continue; // Key already in map
                }
                elem = entry.getValue();
                if (elem.isJsonPrimitive())
                {
                    catMessages.put(entry.getKey(), ChatFormat.parseFormats(elem.getAsString()));
                }
            }
        }
    }

    @Override
    public void clean()
    {
        this.messages.clear();
    }
}
