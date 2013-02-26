package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This class provides functionality to translate messages.
 */
public class I18n implements Cleanable
{
    private static final Logger LOGGER = new CubeLogger("language");
    public static final SourceLanguage SOURCE_LANGUAGE = SourceLanguage.getInstance();
    private final Map<String, Language> languageMap;
    private String defaultLanguage;

    public I18n(Core core)
    {
        this.languageMap = new THashMap<String, Language>();
        this.registerLanguage(SOURCE_LANGUAGE);

        this.defaultLanguage = core.getConfiguration().defaultLanguage;
        FileManager fm = core.getFileManager();
        this.loadLanguages(fm.getLanguageDir());
        try
        {
            LOGGER.addHandler(new CubeFileHandler(LogLevel.ALL, new File(fm.getLogDir(), "missing-translations").getPath()));
        }
        catch (IOException e)
        {
            core.getCoreLogger().log(ERROR, e.getLocalizedMessage(), e);
        }
    }

    public Set<Language> getLanguages()
    {
        return new THashSet<Language>(this.languageMap.values());
    }

    /**
     * Returns the default language
     *
     * @return the locale string of the default language
     */
    public String getDefaultLanguage()
    {
        return this.defaultLanguage;
    }

    /**
     * Sets the default language
     *
     * @param language the new default language
     */
    public void setDefaultLanguage(String language)
    {
        Validate.notNull(language, "The language must not be null!");

        language = normalizeLanguage(language);
        if (SOURCE_LANGUAGE.equals(language) || this.languageMap.containsKey(language))
        {
            this.defaultLanguage = language;
        }
    }

    /**
     * This method load all languages from a directory
     *
     * @param languageDir the directory to load from
     */
    private void loadLanguages(File languageDir)
    {
        Map<String, LanguageConfiguration> languages = new HashMap<String, LanguageConfiguration>();
        LanguageConfiguration config;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.YAML))
        {
            config = Configuration.load(LanguageConfiguration.class, file, false);
            config.code = normalizeLanguage(config.code);
            if (config.code != null)
            {
                languages.put(config.code, config);
            }
            else
            {
                LOGGER.log(ERROR, "The language ''{0}'' has an invalid configuration!", file.getName());
            }
        }

        Stack<String> loadStack = new Stack<String>();
        for (LanguageConfiguration entry : languages.values())
        {
            this.loadLanguage(languageDir, entry, languages, loadStack);
        }
    }

    private Language loadLanguage(File languageDir, LanguageConfiguration config, Map<String, LanguageConfiguration> languages, Stack<String> loadStack)
    {
        if (this.languageMap.containsKey(config.code))
        {
            return this.languageMap.get(config.code);
        }
        if (loadStack.contains(config.code))
        {
            LOGGER.log(ERROR, "The language ''{0}'' caused a circular dependency!", loadStack.peek());
            return null;
        }
        Language language = null;
        config.parent = normalizeLanguage(config.parent);
        if (SOURCE_LANGUAGE.equals(config.parent))
        {
            language = SOURCE_LANGUAGE;
        }
        else
        {
            LanguageConfiguration parent = languages.get(config.parent);
            if (parent != null)
            {
                loadStack.add(config.code);
                language = this.loadLanguage(languageDir, parent, languages, loadStack);
                loadStack.pop();
            }
        }
        try
        {
            language = new NormalLanguage(config, languageDir, language);
            this.registerLanguage(language);
            if (config.clones != null)
            {
                Language clonedLanguage;
                for (String clone : config.clones)
                {
                    clonedLanguage = ClonedLanguage.clone(language, clone);
                    if (clonedLanguage != null && !SOURCE_LANGUAGE.equals(clonedLanguage.getCode()))
                    {
                        this.registerLanguage(clonedLanguage);
                    }
                }
            }

            return language;
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.log(ERROR, "Failed to load the language ''{0}'': {1}", new Object[] {
                config.code, e.getLocalizedMessage()
            });
        }
        return null;
    }

    private void registerLanguage(Language language)
    {
        this.languageMap.put(language.getCode().toLowerCase(SOURCE_LANGUAGE.getLocale()), language);
        this.languageMap.put(language.getName().toLowerCase(SOURCE_LANGUAGE.getLocale()), language);
        this.languageMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);
    }

    /**
     * Returns a language by their locale string / name
     *
     * @param name the name / locale string
     * @return the language or null if not found
     */
    public Language getLanguage(String name)
    {
        return this.getLanguage(name, SOURCE_LANGUAGE.getLocale());
    }

    public Language getLanguage(String name, Locale locale)
    {
        if (name == null)
        {
            return null;
        }
        if (name.indexOf('_') != -1)
        {
            name = normalizeLanguage(name);
        }
        return this.languageMap.get(name.toLowerCase(locale));
    }

    public Set<Language> searchLanguages(String name)
    {
        return this.searchLanguages(name, 2);
    }

    public Set<Language> searchLanguages(String name, int maximumEditDistance)
    {
        Set<String> matches = Match.string().getBestMatches(name, this.languageMap.keySet(), maximumEditDistance);
        Set<Language> languages = new THashSet<Language>(matches.size());

        for (String match : matches)
        {
            languages.add(this.getLanguage(match));
        }

        return languages;
    }

    /**
     * This method translates a messages
     *
     * @param language the language to translate to
     * @param category the category to load the messages from
     * @param message  the message to translate
     * @param params   the parameters to insert into the language after translation
     * @return the translated language
     */
    public String translate(String language, String category, String message, Object... params)
    {
        Validate.notNull(language, "The language must not be null!");
        Validate.notNull(category, "The category must not be null!");

        if (category.isEmpty())
        {
            return message;
        }
        Validate.notNull(params, "The params must not be null!");
        Locale locale = SOURCE_LANGUAGE.getLocale();
        if (message == null)
        {
            return null;
        }

        String translation = null;
        Language lang = this.languageMap.get(language);
        if (lang != null)
        {
            locale = lang.getLocale();
            translation = lang.getTranslation(category, message);
        }

        if (translation == null)
        {
            this.logMissingTranslation(language, category, message);
            Language defLang = this.getLanguage(this.defaultLanguage);
            if (defLang != null)
            {
                translation = defLang.getTranslation(category, message);
            }
            else
            {
                LOGGER.log(WARNING, "The configured default language {0} was not found! Switching back to the source language...", this.defaultLanguage);
                this.defaultLanguage = SOURCE_LANGUAGE.getCode();
            }
            if (translation == null)
            {
                translation = SOURCE_LANGUAGE.getTranslation(category, message);
            }
        }

        if (params.length > 0)
        {
            // Gets Formatted with this: http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html
            return String.format(locale, translation, params);
        }
        return translation;
    }

    @Override
    public void clean()
    {
        for (Language language : this.languageMap.values())
        {
            language.clean();
        }
        SOURCE_LANGUAGE.clean();
        this.languageMap.clear();
    }

    /**
     * This method normalizes a locale string as good as possible
     *
     * @param name the locale string
     * @return the normalized locale string
     */
    public static String normalizeLanguage(String name)
    {
        if (name == null)
        {
            return null;
        }
        final int length = name.length();
        if (length >= 2 && length <= 5)
        {
            final int delimPos = name.indexOf('_');
            if (delimPos < 0 && length == 2)
            {
                return name.toLowerCase(Locale.ENGLISH) + "_" + name.toUpperCase(Locale.ENGLISH);
            }
            else
            {
                if (delimPos == 2 && length == 5)
                {
                    return name.substring(0, 2).toLowerCase(Locale.ENGLISH) + '_' + name.substring(3).replace("_", "").toUpperCase(Locale.ENGLISH);
                }
            }
        }
        return null;
    }

    private void logMissingTranslation(String language, String category, String message)
    {
        LOGGER.log(LogLevel.INFO, String.format("\"%s\" - \"%s\" - \"%s\"", language, category, message));
    }

    private static final Object[] NO_PARAMS = {};

    public static String _(CommandSender sender, Module module, String message, Object[] params)
    {
        return _(sender, module.getId(), message, params);
    }

    public static String _(CommandSender sender, Module module, String message)
    {
        return _(sender, module, message, NO_PARAMS);
    }

    public static String _(CommandSender sender, String category, String message, Object[] params)
    {
        return _(sender.getLanguage(), category, message, params);
    }

    public static String _(CommandSender sender, String category, String message)
    {
        return _(sender, category, message, NO_PARAMS);
    }

    public static String _(Module module, String message, Object[] params)
    {
        return _(module.getId(), message, params);
    }

    public static String _(Module module, String message)
    {
        return _(module, message, NO_PARAMS);
    }

    public static String _(String category, String message, Object[] params)
    {
        return _(CubeEngine.getI18n().getDefaultLanguage(), category, message, params);
    }

    public static String _(String category, String message)
    {
        return _(category, message, NO_PARAMS);
    }

    public static String _(String language, String category, String message, Object[] params)
    {
        return CubeEngine.getI18n().translate(language, category, message, params);
    }

    public static String _(String language, String category, String message)
    {
        return _(language, category, message, NO_PARAMS);
    }
}
