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

import static java.util.stream.Collectors.toList;
import static org.cubeengine.dirigent.context.Contexts.LOCALE;
import static org.cubeengine.dirigent.context.Contexts.createContext;
import static org.cubeengine.libcube.service.i18n.Properties.SOURCE;
import static org.spongepowered.api.Sponge.getAssetManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import org.cubeengine.libcube.service.i18n.formatter.VectorFormatter;
import org.cubeengine.libcube.service.i18n.formatter.WorldFormatter;
import org.cubeengine.libcube.service.logging.LogProvider;
import org.cubeengine.libcube.service.matcher.StringMatcher;
import org.cubeengine.logscribe.Log;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.plugin.PluginContainer;

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
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@Singleton
public class I18n extends I18nTranslate
{
    private final I18nService service;
    private List<URL> poFiles = new LinkedList<>();
    private Map<String, Language> languageLookupMap = new HashMap<>();
    private BuilderDirigent<Component, TextComponent.Builder> compositor;
    @Inject private StringMatcher stringMatcher;
    private final I18nConfig config;
    private final Context defaultContext;

    private final Log log;
    private final LibCube plugin;

    @Inject
    public I18n(FileManager fm, Reflector reflector, LogProvider logProvider, ModuleManager mm)
    {
        this.log = logProvider.getLogger(I18n.class, "I18n", false);
        this.plugin = ((LibCube) mm.getModule(LibCube.class));
        this.config = reflector.load(I18nConfig.class, fm.getDataPath().resolve("i18n.yml").toFile());
        reflector.getDefaultConverterManager().registerConverter(new PluralExprConverter(), PluralExpr.class);

        this.addPoFilesFromDirectory(fm.getTranslationPath());

        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.poFiles);
        I18nLanguageLoader languageLoader = new I18nLanguageLoader(reflector, fm, log);
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

    public void enable()
    {
        LanguageLoader languageLoader = service.getLanguageLoader();
        Asset langs = getAssetManager().getAsset(plugin.getContainer(), "languages/languages.yml").get();
        try
        {
            List<URL> urls = new ArrayList<>();
            for (String lang : langs.readLines())
            {
                Optional<Asset> langAsset = getAssetManager().getAsset(plugin.getContainer(), "languages/" + lang + ".yml");
                if (langAsset.isPresent())
                {
                    urls.add(langAsset.get().getUrl());
                }
                else
                {
                    log.warn("Could not find language definition for: " + lang);
                }
            }
            if (urls.size() != 0)
            {
                log.info("Loading {} language definitions", urls.size());
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
        String name = plugin.getMetadata().getName().orElse(plugin.getMetadata().getId());
        for (Language language : getLanguages())
        {
            String lang = language.getLocale().getLanguage();
            String full = lang + "_ " + language.getLocale().getCountry();
            Optional<Asset> asset = getAssetManager().getAsset(plugin, "translations/" + lang + "_" + name + ".po");
            asset.map(Asset::getUrl).ifPresent(poFiles::add);
            asset = getAssetManager().getAsset(plugin, "translations/" + full + "_" + name + ".po");
            asset.map(Asset::getUrl).ifPresent(poFiles::add);
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
            final Audience audience = ((CommandCause) receiver).getAudience();
            if (audience instanceof LocaleSource) {
                defaultContext.set(LOCALE.with(((LocaleSource) audience).getLocale()));
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
                log.error(e, "Error while getting translation override files!");
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
            log.error(e, "Error while getting Language!");
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
