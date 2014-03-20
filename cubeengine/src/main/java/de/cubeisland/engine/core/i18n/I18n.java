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
package de.cubeisland.engine.core.i18n;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.i18n.DefinitionLoadingException;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.TranslationLoadingException;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.i18n.loader.GettextLoader;
import gnu.trove.set.hash.THashSet;

public class I18n
{
    final Core core;
    private final I18nService service;
    private List<URI> translationFolders = new LinkedList<>();

    public I18n(Core core)
    {
        this.core = core;
        // TODO fill translationFolders
        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.translationFolders);
        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, new I18nLanguageLoader(core), core.getConfiguration().defaultLocale);
    }

    public String translate(String message)
    {
        return this.translate(this.core.getConfiguration().defaultLocale, message);
    }

    public String translate(Locale locale, String message)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (message == null)
        {
            return null;
        }

        String translation = null;
        Language language = this.getLanguage(locale);
        if (language != null)
        {
            translation = language.getTranslation(message);
        }
        if (translation == null)
        {
            // TODO this.logMissingTranslation(locale, message); still necessary?
            Language defLang = this.getDefaultLanguage();
            if (defLang != null)
            {
                translation = defLang.getTranslation(message);
            }
            else
            {
                this.core.getLog().warn("The configured default language {} was not found! Falling back to the source language...", this.core
                    .getConfiguration().defaultLocale.getDisplayName());
            }
            if (translation == null)
            {
                translation = service.getSourceLanguage().getTranslation(message); // TODO why not just return the message?
            }
        }
        return translation;
    }

    public Language getLanguage(Locale locale)
    {
        try
        {
            return this.service.getLanguage(locale);
        }
        catch (TranslationLoadingException | DefinitionLoadingException e)
        {
            this.core.getLog().error(e, "Error while getting Language!");
            return null;
        }
    }

    public Language getDefaultLanguage()
    {
        return this.getLanguage(service.getDefaultLocale());
    }

    public Set<Language> getLanguages()
    {
        // TODO this does no longer returns all languages available but only all currently loaded languages!
        return new THashSet<>(this.service.getLoadedLanguages());
    }


    private static boolean mayBeRegionCode(String string)
    {
        if (!StringUtils.isNumeric(string))
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
        String country = ""; // TODO redundant

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
                country = country.substring(0, 2); // TODO never used
            }
        }

        language = language.toLowerCase(Locale.US);
        country = language.toUpperCase(Locale.US);

        return new Locale(language, country);
    }

    /*
        TODO language names & searchLangMethods
        this.languageLookupMap.put(localeToString(language.getLocale()), language);
        this.languageLookupMap.put(language.getName().toLowerCase(language.getLocale()), language);
        this.languageLookupMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);

        public Set<Language> searchLanguages(String name)
        {
            return this.searchLanguages(name, 2);
        }

        public Set<Language> searchLanguages(String name, int maximumDifference)
        {
            Set<String> matches = Match.string().getBestMatches(name, this.languageLookupMap.keySet(), maximumDifference);
            Set<Language> languages = new THashSet<>(matches.size());

            for (String match : matches)
            {
                languages.add(this.languageLookupMap.get(match));
            }

            return languages;
        }
     */
}
