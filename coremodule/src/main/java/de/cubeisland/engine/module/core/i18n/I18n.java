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
package de.cubeisland.engine.module.core.i18n;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javax.inject.Inject;
import javax.inject.Provider;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.I18nUtil;
import de.cubeisland.engine.i18n.language.DefinitionLoadingException;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.i18n.loader.GettextLoader;
import de.cubeisland.engine.i18n.plural.PluralExpr;
import de.cubeisland.engine.i18n.translation.TranslationLoadingException;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.messagecompositor.MessageCompositor;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.util.formatter.ColoredMessageCompositor;
import de.cubeisland.engine.module.core.util.matcher.StringMatcher;
import de.cubeisland.engine.reflect.Reflector;
import org.spongepowered.api.text.Text.Translatable;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.translation.Translation;

import static java.util.stream.Collectors.toList;

@ServiceProvider(I18n.class)
public class I18n
{
    private final I18nService service;
    private List<URL> poFiles = new LinkedList<>();
    private Map<String, Language> languageLookupMap = new HashMap<>();
    private ColoredMessageCompositor compositor;

    @Inject
    private Log log;
    private StringMatcher stringMatcher;

    @Inject
    public I18n(FileManager fm, Reflector reflector, StringMatcher stringMatcher)
    {
        this.stringMatcher = stringMatcher;
        reflector.getDefaultConverterManager().registerConverter(new PluralExprConverter(), PluralExpr.class);

        this.addPoFilesFromDirectory(fm.getTranslationPath());

        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.poFiles);
        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, new I18nLanguageLoader(reflector, fm,
                                                                                                       log),
                                       getDefaultLocale());
        this.compositor = new ColoredMessageCompositor(reflector, fm);
    }

    public MessageCompositor getCompositor()
    {
        return compositor;
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
                log.error(e, "Error while getting translation override files!");
            }
        }
    }

    public void registerModule(Module module)
    {
        Path translations = module.getProvided(Path.class).resolve("translations");
        this.addPoFilesFromDirectory(translations);
        this.poFiles.addAll(getFilesFromJar("translations/", ".po", module));
    }

    public static List<URL> getFilesFromJar(String path, String fileEnding, Module module)
    {
        try
        {
            URL sourceURL = module.getProvided(URL.class);
            List<URL> urls = new ArrayList<>();
            Path directory = Paths.get(sourceURL.toURI()).resolve(path);
            if (Files.isDirectory(directory))
            {
                for (Path file : Files.newDirectoryStream(directory, FileExtensionFilter.PO))
                {
                    urls.add(file.toUri().toURL());
                }
                return urls;
            }
            Set<String> files = new LinkedHashSet<>();
            JarFile jarFile = new JarFile(new File(sourceURL.toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && name.endsWith(fileEnding))
                {
                    files.add(name);
                }
            }

            urls.addAll(files.stream().map(file -> module.getClass().getResource("/" + file)).collect(toList()));
            return urls;
        }
        catch (IOException | URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public String translate(BaseFormatting format, String message, Object... args)
    {
        return this.translate(getDefaultLocale(), format, message, args);
    }

    public String translate(Locale locale, BaseFormatting format, String message, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (message == null)
        {
            return null;
        }
        return composeMessage(locale, format, this.translate(locale, message), args);
    }

    public String composeMessage(Locale locale, BaseFormatting format, String message, Object[] args)
    {
        return this.compositor.composeMessage(format, locale, message, args);
    }

    public String translateN(BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        return this.translateN(getDefaultLocale(), format, n, singular, plural, args);
    }

    private Locale getDefaultLocale()
    {
        return Locale.getDefault();
    }

    public String translateN(Locale locale, BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (singular == null || plural == null)
        {
            return null;
        }
        return this.compositor.composeMessage(format, locale, this.translateN(locale, n, singular, plural), args);
    }

    public String translate(String message)
    {
        return this.translate(getDefaultLocale(), message);
    }

    public String translate(Locale locale, String message)
    {
        return this.service.translate(locale, message);
    }

    public String translateN(int n, String singular, String plural)
    {
        return this.translateN(getDefaultLocale(), n, singular, plural);
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
            log.error(e, "Error while getting Language!");
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
        return new HashSet<>(this.service.getLoadedLanguages());
    }

    public Set<Language> searchLanguages(String name, int maxDistance)
    {
        Locale locale = I18nUtil.stringToLocale(name.toLowerCase());
        Language language = this.getLanguage(locale);
        if (language != null)
        {
            HashSet<Language> lang = new HashSet<>();
            lang.add(language);
            return lang;
        }

        Set<String> matches = stringMatcher.getBestMatches(name.toLowerCase(), this.languageLookupMap.keySet(),
                                                           maxDistance);
        Set<Language> languages = new HashSet<>(matches.size());

        for (String match : matches)
        {
            languages.add(this.languageLookupMap.get(match));
        }

        return languages;
    }

    public Translatable getTranslation(BaseFormatting format, Locale locale, String msg, Object... args)
    {
        return Texts.of(new CubeEngineTranslation(this, format, locale, msg, args));
    }

    public Translatable getTranslationN(BaseFormatting format, Locale locale, int n, String singular, String plural,
                                        Object... args)
    {
        return Texts.of(new CubeEngineTranslation(this, format, locale, n, singular, plural, args));
    }

    public I18nService getService()
    {
        return service;
    }
}
