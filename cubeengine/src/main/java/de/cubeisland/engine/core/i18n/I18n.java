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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.formatter.ColoredMessageCompositor;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.I18nUtil;
import de.cubeisland.engine.i18n.language.DefinitionLoadingException;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.i18n.loader.GettextLoader;
import de.cubeisland.engine.i18n.plural.PluralExpr;
import de.cubeisland.engine.i18n.translation.TranslationLoadingException;
import de.cubeisland.engine.messagecompositor.MessageCompositor;
import gnu.trove.set.hash.THashSet;

public class I18n
{
    final Core core;
    private final I18nService service;
    private List<URL> poFiles = new LinkedList<>();
    private Map<String, Language> languageLookupMap = new HashMap<>();
    private ColoredMessageCompositor compositor;

    public MessageCompositor getCompositor()
    {
        return compositor;
    }

    public I18n(Core core)
    {
        core.getConfigFactory().getDefaultConverterManager().registerConverter(PluralExpr.class, new PluralExprConverter());

        this.core = core;
        this.addPoFilesFromDirectory(this.core.getFileManager().getTranslationPath());
        this.poFiles.addAll(getFilesFromJar("translations/", ".po", this.getClass()));

        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.poFiles);
        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, new I18nLanguageLoader(core), core.getConfiguration().defaultLocale);
        this.compositor = new ColoredMessageCompositor(core);
    }

    private void addPoFilesFromDirectory(Path translations)
    {
        if (Files.exists(translations))
        {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(translations, FileExtensionFilter.PO))
            {
                for (Path path : directoryStream)
                {
                    this.poFiles.add(path.toUri().toURL());
                }
            }
            catch (IOException e)
            {
                this.core.getLog().error(e, "Error while getting translation override files!");
            }
        }
    }

    public void registerModule(Module module)
    {
        Path translations = module.getFolder().resolve("translations");
        this.addPoFilesFromDirectory(translations);
        this.poFiles.addAll(getFilesFromJar("translations/", ".po", module.getClass()));
    }

    public static List<URL> getFilesFromJar(String path, String fileEnding, Class clazz)
    {
        try
        {
            URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
            Set<String> files = new LinkedHashSet<>();
            JarFile jarFile = new JarFile(new File(url.toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && name.endsWith(fileEnding))
                {
                    files.add(name);
                }
            }
            List<URL> urls = new ArrayList<>();
            for (String file : files)
            {
                urls.add(clazz.getResource("/" + file));
            }
            return urls;
        }
        catch (IOException | URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
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

    public String translateN(MessageType type, int n, String singular, String plural, Object... args)
    {
        return this.translateN(this.core.getConfiguration().defaultLocale, type, n, singular, plural, args);
    }

    public String translateN(Locale locale, MessageType type, int n, String singular, String plural, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (singular == null || plural == null)
        {
            return null;
        }
        return this.compositor.composeMessage(type, locale, this.translateN(locale, n, singular, plural), args);
    }

    public String translate(String message)
    {
        return this.translate(this.core.getConfiguration().defaultLocale, message);
    }

    public String translate(Locale locale, String message)
    {
        return this.service.translate(locale, message);
    }

    public String translateN(int n, String singular, String plural)
    {
        return this.translateN(this.core.getConfiguration().defaultLocale, n, singular, plural);
    }

    public String translateN(Locale locale, int n, String singular, String plural)
    {
        return this.service.translateN(locale, singular, plural, n);
    }

    public Language getLanguage(Locale locale)
    {
        try
        {
            Language language = this.service.getLanguage(locale);
            if (language != null)
            {
                this.languageLookupMap.put(language.getName().toLowerCase(language.getLocale()), language);
                this.languageLookupMap.put(language.getLocalName().toLowerCase(language.getLocale()), language);
            }
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
