/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.i18n;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.util.Cleanable;

import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

/**
 * This class is a generic language that loads its translations from files.
 */
public class NormalLanguage implements Cleanable, Language
{
    private final Core core;
    private final String name;
    private final String localName;
    private final Language parent;
    private final Map<String, String> messages;
    private final File messageDir;
    private final Locale locale;

    public NormalLanguage(Core core, LocaleConfig config, File languageDir, Language parent)
    {
        assert config.locale != null: "The code must not be null!";
        assert config.name != null: "The name must not be null!";
        assert config.localName != null: "The local name must not be null!";

        this.core = core;

        this.name = config.name;
        this.localName = config.localName;
        this.locale = config.locale;
        this.parent = parent;
        this.messageDir = new File(languageDir, I18n.localeToString(this.locale));
        this.messages = new THashMap<String, String>();
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
        /*
        try
        {
            final File messagesFile = new File(this.messageDir, cat + ".json");
            Map<String, String> moduleMessages = this.objectMapper.readValue(messagesFile, Map.class);
            moduleMessages = this.updateMessages(messagesFile, moduleMessages);

            for (Map.Entry<String, String> translation : moduleMessages.entrySet())
            {
                if (!this.messages.containsKey(translation.getId()))
                {
                    this.messages.put(translation.getId(), ChatFormat.parseFormats(translation.getValue()));
                }
            }
            return moduleMessages;
        }
        catch (FileNotFoundException ignored)
        {
            this.core.getLog().log(LogLevel.WARNING, "The translation category " + cat + " was not found for the language ''" + this.code + "'' !");
        }
        catch (IOException e)
        {
            this.core.getLog().log(LogLevel.ERROR, String.valueOf(e), e);
        }*/
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> updateMessages(File messageFile, Map<String, String> catMessages)
    {
        /*
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
            CubeEngine.getLog().log(LogLevel.WARNING, e.getLocalizedMessage(), e);
        }
        finally
        {
            try
            {
                resource.close();
            }
            catch (IOException ignored)
            {}
        } */
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
        return this.locale.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof NormalLanguage))
        {
            return false;
        }
        return this.locale.equals(((NormalLanguage)obj).locale);
    }
}
