package de.cubeisland.cubeengine.core.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.Resource;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public class NormalLanguage implements Cleanable, Language
{
    private final String code;
    private final String name;
    private final String localName;
    private final Language parent;
    private final Map<String, Map<String, String>> messages;
    private final File messageDir;
    private final JsonParser parser;
    private final Locale locale;

    public NormalLanguage(LanguageConfiguration config, File languageDir, Language parent)
    {
        Validate.notNull(config.code, "The code must not be null!");
        Validate.notNull(config.name, "The name must not be null!");
        Validate.notNull(config.localName, "The local name must not be null!");

        this.code = I18n.normalizeLanguage(config.code);
        Validate.notNull(this.code, "The configured language code is invalid!");

        this.name = config.name;
        this.localName = config.localName;
        this.parent = parent;
        this.messageDir = new File(languageDir, this.code);
        this.messages = new THashMap<String, Map<String, String>>();
        this.parser = new JsonParser();

        this.locale = new Locale(this.code.substring(0, 2), this.code.substring(3, 5));
    }

    @Override
    public String getCode()
    {
        return this.code;
    }

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

    public void addMessages(String cat, Map<String, String> messages)
    {
        Validate.notNull(cat, "The category must not be null!");
        Validate.notNull(messages, "The messages must not be null!");
        
        this.messages.put(cat, messages);
    }

    @Override
    public String getTranslation(String cat, String message)
    {
        String translation = null;
        Map<String, String> catMessages = this.messages.get(cat);
        if (catMessages == null)
        {
            catMessages = this.loadMessages(cat);
        }
        if (catMessages != null)
        {
            translation = catMessages.get(message);
        }
        if (translation == null && parent != null)
        {
            translation = this.parent.getTranslation(cat, message);
        }
        return translation;
    }
    
    @Override
    public Map<String, String> getMessages(String cat)
    {
        if (this.messages.containsKey(cat))
        {
            return Collections.unmodifiableMap(this.messages.get(cat));
        }
        return null;
    }

    public Language getParent()
    {
        return this.parent;
    }

    private Map<String, String> loadMessages(String cat)
    {
        try
        {
            FileReader reader = new FileReader(new File(this.messageDir, cat + ".json"));
            JsonElement root = parser.parse(reader);
            reader.close();
            Map<String, String> catMessages = new THashMap<String, String>();
            if (root.isJsonObject())
            {
                JsonElement elem;
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet())
                {
                    elem = entry.getValue();
                    if (elem.isJsonPrimitive())
                    {
                        catMessages.put(entry.getKey(), ChatFormat.parseFormats(elem.getAsString()));
                    }
                }
            }
            this.updateMessages(cat, catMessages);
            if (!catMessages.isEmpty())
            {
                this.messages.put(cat, catMessages);
                return catMessages;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private void updateMessages(String cat, Map<String, String> catMessages)
    {
        try
        {
            boolean updated = false;
            File file = new File(this.messageDir, cat + ".json");
            Resource resource = CubeEngine.getFileManager().getSourceOf(file);
            if (resource == null)
            {
                return;
            }
            String source = resource.getSource();
            // we only accept absolute paths!
            if (!source.startsWith("/"))
            {
                source = "/" + source;
            }
            InputStreamReader reader = new InputStreamReader(resource.getClass().getResourceAsStream(source));
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
                    updated = true;
                }
            }
            if (updated)
            {
                FileWriter fw = new FileWriter(file);
                StringBuilder sb = new StringBuilder("{\n");
                Iterator<String> iter = catMessages.keySet().iterator();
                String key;
                if (iter.hasNext())
                {
                    key = iter.next();
                    sb.append("    \"").append(key).append("\": \"").append(catMessages.get(key)).append("\"");
                }
                while (iter.hasNext())
                {
                    key = iter.next();
                    sb.append(",\n    \"").append(key).append("\": \"").append(catMessages.get(key)).append("\"");
                }
                sb.append("\n}");
                fw.write(sb.toString());
                fw.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void clean()
    {
        this.messages.clear();
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
}
