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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.util.formatter.ColoredMessageCompositor;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.i18n.DefinitionLoadingException;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.I18nUtil;
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
    private Map<String, Language> languageLookupMap = new HashMap<>();
    private ColoredMessageCompositor compositor;

    public I18n(Core core)
    {
        this.core = core;
        // TODO fill translationFolders
        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.translationFolders);
        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, new I18nLanguageLoader(core), core.getConfiguration().defaultLocale);
        this.compositor = new ColoredMessageCompositor(core);
    }

    public String translate(MessageType type, String message, Object... args)
    {
        return this.translate(this.core.getConfiguration().defaultLocale, type, message, args);
    }

    public String translate(Locale locale, MessageType type, String message, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (message == null)
        {
            return null;
        }
        return this.compositor.composeMessage(type, locale, this.translate(locale, message), args);
    }

    public String translate(String message)
    {
        return this.translate(this.core.getConfiguration().defaultLocale, message);
    }

    public String translate(Locale locale, String message)
    {
        return this.service.translate(locale, message);
    }

    public Language getLanguage(Locale locale)
    {
        try
        {
            Language language = this.service.getLanguage(locale);
            this.languageLookupMap.put(language.getName().toLowerCase(language.getLocale()), language);
            this.languageLookupMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);
            return language;
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

    public Set<Language> searchLanguages(String name, int maxDistance)
    {
        Locale locale = I18nUtil.stringToLocale(name);
        Language language = this.getLanguage(locale);
        if (language != null)
        {
            HashSet<Language> lang = new HashSet<>();
            lang.add(language);
            return lang;
        }

        Set<String> matches = Match.string().getBestMatches(name.toLowerCase(), this.languageLookupMap.keySet(), maxDistance);
        Set<Language> languages = new THashSet<>(matches.size());

        for (String match : matches)
        {
            languages.add(this.languageLookupMap.get(match));
        }

        return languages;
    }
}
