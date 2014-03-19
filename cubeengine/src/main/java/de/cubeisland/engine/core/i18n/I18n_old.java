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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.logging.LoggingUtil;
import de.cubeisland.engine.core.util.Cleanable;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;


/**
 * This class provides functionality to translate messages.
 */
public class I18n_old implements Cleanable
{
    private final Map<Locale, Language> languages;
    private final Map<String, Language> languageLookupMap;

    public I18n_old(Core core)
    {
        this.languages = new THashMap<>();
        this.languageLookupMap = new THashMap<>();

        this.languages.put(this.sourceLanguage.getLocale(), this.sourceLanguage);
        this.registerLanguage(this.sourceLanguage);

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
        return new THashSet<>(this.languages.values());
    }


    /**
     * Returns the default language
     *
     * @return the locale string of the default language
     */


    private void registerLanguage(Language language)
    {
        this.languageLookupMap.put(localeToString(language.getLocale()), language);
        this.languageLookupMap.put(language.getName().toLowerCase(language.getLocale()), language);
        this.languageLookupMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);
    }

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

    private void logMissingTranslation(Locale locale, String message)
    {
        this.logger.info("\"{}\" \"{}\"", localeToString(locale), message);
    }

    @Override
    public void clean()
    {
        this.languageLookupMap.clear();
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
