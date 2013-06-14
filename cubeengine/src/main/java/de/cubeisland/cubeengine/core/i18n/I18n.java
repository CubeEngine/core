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
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.filesystem.gettext.MessageCatalogFactory;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Misc;
import de.cubeisland.cubeengine.core.util.matcher.Match;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.LoggerFactory;

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
    private final Map<Locale, Language> languages;
    private final Map<String, Language> languageLookupMap;
    private Locale defaultLocale;
    private final MessageCatalogFactory messageCatalogFactory;

    public I18n(Core core)
    {
        this.core = core;
        this.logger = (Logger) LoggerFactory.getLogger("cubeengine.language");
        // TODO
        this.languages = new THashMap<Locale, Language>();
        this.languageLookupMap = new THashMap<String, Language>();
        this.sourceLanguage = new SourceLanguage();
        this.languages.put(this.sourceLanguage.getLocale(), this.sourceLanguage);
        this.registerLanguage(this.sourceLanguage);
        this.messageCatalogFactory = new MessageCatalogFactory();

        FileManager fm = core.getFileManager();
        this.loadLanguages(fm.getLanguageDir());

        Locale def = core.getConfiguration().defaultLocale;
        if (this.languages.containsKey(def))
        {
            Locale.setDefault(def);
        }
        else
        {
            Locale.setDefault(this.sourceLanguage.getLocale());
            core.getConfiguration().defaultLocale = Locale.getDefault();
        }
    }

    public Set<Language> getLanguages()
    {
        return new THashSet<Language>(this.languages.values());
    }

    public SourceLanguage getSourceLanguage()
    {
        return this.sourceLanguage;
    }

    /**
     * Returns the default language
     *
     * @return the locale string of the default language
     */
    public Language getDefaultLanguage()
    {
        Language language = this.languages.get(Locale.getDefault());
        if (language == null)
        {
            language = this.sourceLanguage;
        }
        return language;
    }

    /**
     * This method load all languages from a directory
     *
     * @param languageDir the directory to load from
     */
    private synchronized void loadLanguages(File languageDir)
    {
        Map<Locale, LocaleConfig> languages = new HashMap<Locale, LocaleConfig>();
        LocaleConfig config;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.YAML))
        {
            config = Configuration.load(LocaleConfig.class, file, false);
            if (config.locale != null)
            {
                languages.put(config.locale, config);
            }
            else
            {
                this.logger.error("The language ''{}'' has an invalid configuration!", file.getName());
            }
        }

        Stack<Locale> loadStack = new Stack<Locale>();
        for (LocaleConfig entry : languages.values())
        {
            this.loadLanguage(languageDir, entry, languages, loadStack);
        }
    }

    private Language loadLanguage(File languageDir, LocaleConfig config, Map<Locale, LocaleConfig> languages, Stack<Locale> loadStack)
    {
        if (this.languages.containsKey(config.locale))
        {
            return this.languages.get(config.locale);
        }
        if (loadStack.contains(config.locale))
        {
            this.logger.error("The language ''{}'' caused a circular dependency!", loadStack.peek());
            return null;
        }
        Language language = null;

        if (config.parent != null && this.sourceLanguage.equals(config.parent))
        {
            language = this.sourceLanguage;
        }
        else
        {
            LocaleConfig parent = languages.get(config.parent);
            if (parent != null)
            {
                loadStack.add(config.locale);
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
                for (Locale cloneLocale : config.clones)
                {
                    clonedLanguage = ClonedLanguage.clone(cloneLocale, language);
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
            this.logger.error("Failed to load the language ''{}'': {}", config.locale, e.getLocalizedMessage());
        }
        return null;
    }

    private void registerLanguage(Language language)
    {
        this.languageLookupMap.put(localeToString(language.getLocale()), language);
        this.languageLookupMap.put(language.getName().toLowerCase(language.getLocale()), language);
        this.languageLookupMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);
    }

    /**
     * Returns a language by their locale string / name
     *
     * @param locale the locale
     * @return the language or null if not found
     */
    public Language getLanguage(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException("The locale must not be null!");
        }
        return this.languages.get(locale);
    }

    public Set<Language> searchLanguages(String name)
    {
        return this.searchLanguages(name, 2);
    }

    public Set<Language> searchLanguages(String name, int maximumDifference)
    {
        Set<String> matches = Match.string().getBestMatches(name, this.languageLookupMap.keySet(), maximumDifference);
        Set<Language> languages = new THashSet<Language>(matches.size());

        for (String match : matches)
        {
            languages.add(this.languageLookupMap.get(match));
        }

        return languages;
    }

    private void logMissingTranslation(Locale locale, String message)
    {
        this.logger.info(String.format("\"%s\" \"%s\"", localeToString(locale), message));
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
        Language lang = this.languageLookupMap.get(localeToString(locale)); // TODO locale -> string -> language WTF?
        if (lang != null)
        {
            locale = lang.getLocale();
            translation = lang.getTranslation(message);
        }

        if (translation == null)
        {
            this.logMissingTranslation(locale, message);
            Language defLang = this.getLanguage(Locale.getDefault());
            if (defLang != null)
            {
                translation = defLang.getTranslation(message);
                locale = Locale.getDefault();
            }
            else
            {
                this.logger.warn("The configured default language {} was not found! Falling back to the source language...", this
                    .defaultLocale.getDisplayName());
                locale = this.defaultLocale = this.sourceLanguage.getLocale();
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
        for (Language language : this.languageLookupMap.values())
        {
            language.clean();
        }
        this.sourceLanguage.clean();
        this.languageLookupMap.clear();
    }

    public static String localeToString(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException("The locale must not be null!");
        }
        return locale.getLanguage().toLowerCase(Locale.US) + '_' + locale.getCountry().toUpperCase(Locale.US);
    }

    private static boolean mayBeRegionCode(String string)
    {
        if (!Misc.isNumeric(string))
        {
            return false;
        }
        try
        {
            int countryCode = Integer.parseInt(string);
            if (countryCode <= 999)
            {
                return true;
            }
        }
        catch (NumberFormatException ignored)
        {}
        return false;
    }

    public static Locale stringToLocale(String string)
    {
        if (string == null)
        {
            return Locale.getDefault();
        }
        string = string.trim();
        if (string.isEmpty())
        {
            return Locale.getDefault();
        }

        string = string.replace('-', '_').replaceAll("[^a-z0-9_]", "");
        String[] parts = string.split("_", 2);

        String language = parts[0];
        String country = "";

        // if the language code is longer than 3-alpha's
        if (language.length() > 3)
        {
            // strip it to a 2-alpha code
            language = language.substring(0, 2);
        }
        if (parts.length > 0)
        {
            country = parts[1];
            if (country.length() > 2 && !mayBeRegionCode(country))
            {
                country = country.substring(0, 2);
            }
        }

        language = language.toLowerCase(Locale.US);
        country = language.toUpperCase(Locale.US);

        return new Locale(language, country);
    }

    /**
     * Use this method to mark strings as translatable
     *
     * @param string the string to mark
     * @return the exact same string
     */
    @Deprecated
    public static String _(String string)
    {
        return string;
    }
}
