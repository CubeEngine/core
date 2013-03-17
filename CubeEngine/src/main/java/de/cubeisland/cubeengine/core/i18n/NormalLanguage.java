package de.cubeisland.cubeengine.core.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.Core;
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
import java.util.Locale;
import java.util.Map;

/**
 * This class is a generic language that loads its translations from files.
 */
public class NormalLanguage implements Cleanable, Language
{
    private final Core core;
    private final String code;
    private final String name;
    private final String localName;
    private final Language parent;
    private final Map<String, String> messages;
    private final File messageDir;
    private final Locale locale;
    private final ObjectMapper objectMapper = CubeEngine.getJsonObjectMapper();

    public NormalLanguage(Core core, LocaleConfig config, File languageDir, Language parent)
    {
        Validate.notNull(config.code, "The code must not be null!");
        Validate.notNull(config.name, "The name must not be null!");
        Validate.notNull(config.localName, "The local name must not be null!");

        this.core = core;
        this.code = I18n.normalizeLanguage(config.code);
        Validate.notNull(this.code, "The configured language code is invalid!");

        this.name = config.name;
        this.localName = config.localName;
        this.parent = parent;
        this.messageDir = new File(languageDir, this.code);
        this.messages = new THashMap<String, String>();

        this.locale = new Locale(this.code.substring(0, 2), this.code.substring(3, 5));
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
     * @param messages the translations
     */
    public void addMessages(Map<String, String> messages)
    {
        if (messages == null)
        {
            throw new NullPointerException("The messages must not be null!");
        }

        this.messages.putAll(messages);
    }

    @Override
    public String getTranslation(String message)
    {
        String translation = this.messages.get(message);
        if (translation == null && parent != null)
        {
            translation = this.parent.getTranslation(message);
        }
        return translation;
    }

    @Override
    public Map<String, String> getMessages()
    {
        return new THashMap<String, String>(this.messages);
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
    private synchronized Map<String, String> loadMessages(String cat)
    {
        try
        {
            final File messagesFile = new File(this.messageDir, cat + ".json");
            Map<String, String> moduleMessages = this.objectMapper.readValue(messagesFile, Map.class);
            moduleMessages = this.updateMessages(messagesFile, moduleMessages);

            for (Map.Entry<String, String> translation : moduleMessages.entrySet())
            {
                if (!this.messages.containsKey(translation.getKey()))
                {
                    this.messages.put(translation.getKey(), ChatFormat.parseFormats(translation.getValue()));
                }
            }
            return moduleMessages;
        }
        catch (FileNotFoundException ignored)
        {
            this.core.getCoreLogger().log(LogLevel.WARNING, "The translation category " + cat + " was not found for the language ''" + this.code + "'' !");
        }
        catch (IOException e)
        {
            this.core.getCoreLogger().log(LogLevel.ERROR, String.valueOf(e), e);
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
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof NormalLanguage))
        {
            return false;
        }
        return this.code.equals(((NormalLanguage)obj).code);
    }
}
