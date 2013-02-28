package de.cubeisland.cubeengine.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * This class is a generic language that loads its translations from files.
 */
public class NormalLanguage implements Cleanable, Language
{
    private final String code;
    private final String name;
    private final String localName;
    private final Language parent;
    private final Map<String, Map<String, String>> messages;
    private final File messageDir;
    private final Locale locale;
    private final ObjectMapper objectMapper = CubeEngine.getJsonObjectMapper();

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

    /**
     * This method adds a map of translations to a category
     *
     * @param cat      the category
     * @param messages the translations
     */
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

    /**
     * Returns the language's parent
     *
     * @return the parent language
     */
    public Language getParent()
    {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadMessages(String cat)
    {
        try
        {
            final File messagesFile = new File(this.messageDir, cat + ".json");
            Map<String, String> catMessages = this.objectMapper.readValue(messagesFile, Map.class);
            catMessages = this.updateMessages(messagesFile, catMessages);

            for (Map.Entry<String, String> translation : catMessages.entrySet())
            {
                translation.setValue(ChatFormat.parseFormats(translation.getValue()));
            }

            if (!catMessages.isEmpty())
            {
                this.messages.put(cat, catMessages);
                return catMessages;
            }
        }
        catch (FileNotFoundException ignored)
        {
            CubeEngine.getLogger().log(LogLevel.WARNING, "The translation category " + cat + " was not found for the language ''" + this.code + "'' !");
        }
        catch (IOException e)
        {
            CubeEngine.getLogger().log(LogLevel.ERROR, String.valueOf(e), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> updateMessages(File messageFile, Map<String, String> catMessages)
    {
        InputStream resource = CubeEngine.getFileManager().getSourceOf(messageFile);
        if (resource == null)
        {
            return catMessages;
        }
        try
        {
            Map<String, String> newMessages = this.objectMapper.readValue(resource, Map.class);
            newMessages.putAll(catMessages);
            if (newMessages.size() != catMessages.size())
            {
                this.objectMapper.writeValue(messageFile, newMessages);
            }
        }
        catch (IOException e)
        {
            CubeEngine.getLogger().log(LogLevel.WARNING, e.getLocalizedMessage(), e);
        }
        finally
        {
            try
            {
                resource.close();
            }
            catch (IOException ignored)
            {}
        }
        return catMessages;
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof NormalLanguage))
        {
            return false;
        }
        return this.code.equals(((NormalLanguage)obj).code);
    }
}
