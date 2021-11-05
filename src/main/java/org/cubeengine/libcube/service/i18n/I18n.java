/*
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
package org.cubeengine.libcube.service.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cubeengine.dirigent.builder.BuilderDirigent;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.i18n.I18nService;
import org.cubeengine.i18n.I18nUtil;
import org.cubeengine.i18n.language.DefinitionLoadingException;
import org.cubeengine.i18n.language.Language;
import org.cubeengine.i18n.language.LanguageLoader;
import org.cubeengine.i18n.language.SourceLanguage;
import org.cubeengine.i18n.loader.GettextLoader;
import org.cubeengine.i18n.plural.PluralExpr;
import org.cubeengine.i18n.translation.TranslationLoadingException;
import org.cubeengine.libcube.LibCube;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.filesystem.FileExtensionFilter;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.libcube.service.i18n.formatter.BiomeFormatter;
import org.cubeengine.libcube.service.i18n.formatter.BooleanFormatter;
import org.cubeengine.libcube.service.i18n.formatter.ColorPostProcessor;
import org.cubeengine.libcube.service.i18n.formatter.CommandSourceFormatter;
import org.cubeengine.libcube.service.i18n.formatter.ContextFormatter;
import org.cubeengine.libcube.service.i18n.formatter.NumberFormatter;
import org.cubeengine.libcube.service.i18n.formatter.StringFormatter;
import org.cubeengine.libcube.service.i18n.formatter.TextMacro;
import org.cubeengine.libcube.service.i18n.formatter.UrlFormatter;
import org.cubeengine.libcube.service.i18n.formatter.VectorFormatter;
import org.cubeengine.libcube.service.i18n.formatter.WorldFormatter;
import org.cubeengine.libcube.service.matcher.StringMatcher;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.plugin.PluginContainer;

import static java.util.stream.Collectors.toList;
import static org.cubeengine.dirigent.context.Contexts.LOCALE;
import static org.cubeengine.dirigent.context.Contexts.createContext;
import static org.cubeengine.libcube.service.i18n.Properties.SOURCE;


@Singleton
public class I18n extends I18nTranslate
{

    private final I18nService service;
    private final List<URL> poFiles = new LinkedList<>();
    private final Map<String, Language> languageLookupMap = new HashMap<>();
    private final BuilderDirigent<Component, TextComponent.Builder> compositor;
    private final StringMatcher stringMatcher;
    private final I18nConfig config;
    private final Context defaultContext;

    private final LibCube plugin;
    private final Logger logger;

    @Inject
    public I18n(FileManager fm, Reflector reflector, ModuleManager mm, StringMatcher stringMatcher)
    {
        this.logger = LogManager.getLogger(getClass());
        this.stringMatcher = stringMatcher;
        this.plugin = ((LibCube) mm.getModule(LibCube.class));
        this.config = reflector.load(I18nConfig.class, fm.getDataPath().resolve("i18n.yml").toFile());
        reflector.getDefaultConverterManager().registerConverter(new PluralExprConverter(), PluralExpr.class);

        this.addPoFilesFromDirectory(fm.getTranslationPath());

        GettextLoader translationLoader = new GettextLoader(StandardCharsets.UTF_8, this.poFiles);
        I18nLanguageLoader languageLoader = new I18nLanguageLoader(reflector, fm, logger);
        Locale defaultLocale = config.defaultLocale;
        if (defaultLocale == null)
        {
            defaultLocale = Locale.getDefault();
        }

        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, languageLoader, defaultLocale);

        // Search for languages on classPath
        // TODO use Sponge assets?
        ClassLoader classLoader = getClass().getClassLoader();
// TODO find language/translation files
//        try
//        {
//            for (URL url : ((URLClassLoader) classLoader).getURLs())
//            {
//                try
//                {
//                    URI uri = url.toURI();
//                    if (uri.getScheme().equals("file"))
//                    {
//                        languageLoader.loadLanguages(I18n.getFilesFromURL("languages/", ".yml", classLoader, url));
//                        this.poFiles.addAll(getFilesFromURL("translations/", ".po", classLoader, url));
//                    }
//                }
//                catch (IOException ex)
//                {
//                    log.error(ex, "Failed to load language configurations!");
//                }
//            }
//        }
//        catch (URISyntaxException e)
//        {
//            throw new IllegalStateException(e);
//        }

        this.compositor = new BuilderDirigent<>(new TextMessageBuilder(service));

        compositor.registerFormatter(new WorldFormatter());
        compositor.registerFormatter(new StringFormatter());
        compositor.registerFormatter(new UrlFormatter());
        compositor.registerFormatter(new BooleanFormatter(this.service));
        compositor.registerFormatter(new NumberFormatter());
        compositor.registerFormatter(new CommandSourceFormatter(this.service));
        compositor.registerFormatter(new TextMacro());
        compositor.registerFormatter(new BiomeFormatter());
        compositor.registerFormatter(new VectorFormatter());
        compositor.registerFormatter(new ContextFormatter());

        compositor.addPostProcessor(new ColorPostProcessor());

        defaultContext = createContext(LOCALE.with(defaultLocale));
    }

    public static URI pathToURI(Path path)
    {
        return URI.create(StreamSupport.stream(path.spliterator(), false).map(Path::toString).collect(Collectors.joining("/")));
    }

    public void enable()
    {
        LanguageLoader languageLoader = service.getLanguageLoader();
        final Path languagesDir = Paths.get("assets", "cubeengine-core", "languages");
        final URI langs = plugin.getContainer().locateResource(pathToURI(languagesDir.resolve("languages.yml"))).get();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(langs.toURL().openStream()));)
        {
            List<URL> urls = new ArrayList<>();
            String lang;
            while ((lang = reader.readLine()) != null)
            {
                final Optional<URI> langAsset = plugin.getContainer().locateResource(pathToURI(languagesDir.resolve(lang + ".yml")));
                if (langAsset.isPresent())
                {
                    urls.add(langAsset.get().toURL());
                }
                else
                {
                    logger.warn("Could not find language definition for: " + lang);
                }
            }
            if (urls.size() != 0)
            {
                logger.info("Loading {} language definitions", urls.size());
            }
            ((I18nLanguageLoader)languageLoader).loadLanguages(urls);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void registerPlugin(PluginContainer plugin)
    {
        for (Language language : getLanguages())
        {
            String lang = language.getLocale().getLanguage();
            String full = lang + "_" + language.getLocale().getCountry();
            final Path translationsDir = Paths.get("assets", plugin.metadata().id(), "translations");
            plugin.locateResource(pathToURI(translationsDir.resolve(lang + ".po"))).ifPresent(poUri -> {
                try
                {
                    poFiles.add(poUri.toURL());
                }
                catch (MalformedURLException ignored) {}
            });
            plugin.locateResource(translationsDir.resolve(full + ".po").toUri()).ifPresent(poUri -> {
                try
                {
                    poFiles.add(poUri.toURL());
                }
                catch (MalformedURLException ignored) {}
            });
        }
    }

    Context contextFromLocale(Locale locale)
    {
        return defaultContext.set(LOCALE, locale);
    }

    Context contextFromReceiver(Object receiver)
    {
        if (receiver instanceof CommandCause)
        {
            final Audience audience = ((CommandCause) receiver).audience();
            if (audience instanceof LocaleSource) {
                defaultContext.set(LOCALE.with(((LocaleSource) audience).locale()));
            }

            return defaultContext.set(SOURCE.with(((CommandCause) receiver)));
        }

        return defaultContext;
    }

    public BuilderDirigent<Component, TextComponent.Builder> getCompositor()
    {
        return compositor;
    }

    I18nService getI18nService()
    {
        return service;
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
                logger.error("Error while getting translation override files!", e);
            }
        }
    }

    public static List<URL> getFilesFromURL(String path, String fileEnding, ClassLoader classLoader, URL sourceURL)
    {
        try
        {
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
            File fileJar = new File(sourceURL.toURI());
            if (fileJar.isDirectory())
            {
                return urls;
            }
            JarFile jarFile = new JarFile(fileJar);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && name.endsWith(fileEnding))
                {
                    files.add(name);
                }
            }

            urls.addAll(files.stream().map(classLoader::getResource).collect(toList()));
            return urls;
        }
        catch (IOException | URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public Locale getDefaultLocale()
    {
        return service.getDefaultLocale();
    }

    public Language getDefaultLanguage()
    {
        return this.getLanguage(getDefaultLocale());
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
            logger.error("Error while getting Language!", e);
            return null;
        }
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
}
