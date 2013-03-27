package de.cubeisland.cubeengine.core.i18n;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.filesystem.gettext.MessageCatalogFactory;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.matcher.Match;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This class provides functionality to translate messages.
 */
public class I18n implements Cleanable
{
    private final Core core;
    private static final Object[] NO_PARAMS = {};
    private final Logger logger;
    private final SourceLanguage sourceLanguage;
    private final Map<String, Language> languageMap;
    private Locale defaultLocale;
    private final MessageCatalogFactory messageCatalogFactory;

    public I18n(Core core)
    {
        this.core = core;
        this.logger = new CubeLogger("language");
        this.languageMap = new THashMap<String, Language>();
        this.sourceLanguage = new SourceLanguage();
        this.registerLanguage(this.sourceLanguage);
        this.messageCatalogFactory = new MessageCatalogFactory();

        this.defaultLocale = core.getConfiguration().defaultLanguage;
        FileManager fm = core.getFileManager();
        this.loadLanguages(fm.getLanguageDir());
        try
        {
            this.logger.addHandler(new CubeFileHandler(LogLevel.ALL, new File(fm.getLogDir(), "missing-translations").getPath()));
        }
        catch (IOException e)
        {
            core.getLog().log(ERROR, e.getLocalizedMessage(), e);
        }
    }

    public SourceLanguage getSourceLanguage()
    {
        return this.sourceLanguage;
    }

    public Set<Language> getLanguages()
    {
        return new THashSet<Language>(this.languageMap.values());
    }

    public MessageCatalogFactory getMessageCatalogFactory()
    {
        return messageCatalogFactory;
    }

    /**
     * Returns the default language
     *
     * @return the locale string of the default language
     */
    public Language getDefaultLanguage()
    {
        Language language = this.languageMap.get(Locale.getDefault().toString());
        if (language == null)
        {
            language = this.sourceLanguage;
        }
        return language;
    }

    /**
     * Sets the default language
     *
     * @param locale the new default language
     */
    public void setDefaultLocale(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }

        if (this.sourceLanguage.equals(locale) || this.languageMap.containsKey(locale))
        {
            Locale.setDefault(locale);
            this.defaultLocale = locale;
        }
    }

    /**
     * This method load all languages from a directory
     *
     * @param languageDir the directory to load from
     */
    private void loadLanguages(File languageDir)
    {
        Map<String, LocaleConfig> languages = new HashMap<String, LocaleConfig>();
        LocaleConfig config;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.YAML))
        {
            config = Configuration.load(LocaleConfig.class, file, false);
            config.code = normalizeLanguage(config.code);
            if (config.code != null)
            {
                languages.put(config.code, config);
            }
            else
            {
                this.logger.log(ERROR, "The language ''{0}'' has an invalid configuration!", file.getName());
            }
        }

        Stack<String> loadStack = new Stack<String>();
        for (LocaleConfig entry : languages.values())
        {
            this.loadLanguage(languageDir, entry, languages, loadStack);
        }
    }

    private Language loadLanguage(File languageDir, LocaleConfig config, Map<String, LocaleConfig> languages, Stack<String> loadStack)
    {
        if (this.languageMap.containsKey(config.code))
        {
            return this.languageMap.get(config.code);
        }
        if (loadStack.contains(config.code))
        {
            this.logger.log(ERROR, "The language ''{0}'' caused a circular dependency!", loadStack.peek());
            return null;
        }
        Language language = null;
        config.parent = normalizeLanguage(config.parent);
        if (this.sourceLanguage.equals(config.parent))
        {
            language = this.sourceLanguage;
        }
        else
        {
            LocaleConfig parent = languages.get(config.parent);
            if (parent != null)
            {
                loadStack.add(config.code);
                language = this.loadLanguage(languageDir, parent, languages, loadStack);
                loadStack.pop();
            }
        }
        try
        {
            language = new NormalLanguage(this.core, config, languageDir, language);
            this.registerLanguage(language);
            if (config.clones != null)
            {
                Language clonedLanguage;
                for (String clone : config.clones)
                {
                    clonedLanguage = ClonedLanguage.clone(language, clone);
                    if (clonedLanguage != null && !this.sourceLanguage.equals(clonedLanguage.getLocale()))
                    {
                        this.registerLanguage(clonedLanguage);
                    }
                }
            }

            return language;
        }
        catch (IllegalArgumentException e)
        {
            this.logger.log(ERROR, "Failed to load the language ''{0}'': {1}", new Object[]{
                config.code, e.getLocalizedMessage()
            });
        }
        return null;
    }

    private void registerLanguage(Language language)
    {
        this.languageMap.put(language.getLocale().toString(), language);
        this.languageMap.put(language.getName().toLowerCase(language.getLocale()), language);
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
        return this.getLanguage(name, this.sourceLanguage.getLocale());
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
     * @param message  the message to translate
     * @param params   the parameters to insert into the language after translation
     * @return the translated language
     */
    public String translate(String message, Object... params)
    {
        return this.translate(Locale.getDefault(), message, params);
    }

    /**
     * This method translates a messages
     *
     * @param locale the language to translate to
     * @param message  the message to translate
     * @param params   the parameters to insert into the language after translation
     * @return the translated language
     */
    public String translate(Locale locale, String message, Object... params)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (params == null)
        {
            params = NO_PARAMS;
        }
        if (message == null)
        {
            return null;
        }

        String translation = null;
        Language lang = this.languageMap.get(locale);
        if (lang != null)
        {
            locale = lang.getLocale();
            translation = lang.getTranslation(message);
        }

        if (translation == null)
        {
            this.logMissingTranslation(locale, message);
            Language defLang = this.getLanguage(this.defaultLocale.getISO3Language());
            if (defLang != null)
            {
                translation = defLang.getTranslation(message);
            }
            else
            {
                this.logger.log(WARNING, "The configured default language {0} was not found! Falling back to the source language...", this.defaultLocale.getDisplayName());
                this.defaultLocale = this.sourceLanguage.getLocale();
            }
            if (translation == null)
            {
                translation = this.sourceLanguage.getTranslation( message);
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
        this.sourceLanguage.clean();
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

    private void logMissingTranslation(Locale locale, String message)
    {
        this.logger.log(LogLevel.INFO, String.format("\"%s\" \"%s\"", locale.getISO3Country(), message));
    }

    /**
     * This method is only only used to mark strings as translated/translatable.
     * One use-case would be command usages and descriptions.
     *
     * @param string the string to mark
     * @return the unchanged string
     */
    public static String _(String string)
    {
        return string;
    }
}
