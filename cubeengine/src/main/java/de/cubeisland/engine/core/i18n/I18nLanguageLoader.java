package de.cubeisland.engine.core.i18n;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.engine.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.i18n.LanguageLoader;
import de.cubeisland.engine.i18n.language.ClonedLanguage;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.NormalLanguage;

public class I18nLanguageLoader implements LanguageLoader
{
    private final I18n i18n;
    private final Map<Locale, LocaleConfiguration> configurations = new HashMap<>();

    public I18nLanguageLoader(I18n i18n, Path languagePath)
    {
        this.i18n = i18n;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(languagePath, FileExtensionFilter.YAML))
        {
            for (Path path : directoryStream)
            {
                LocaleConfiguration config = i18n.core.getConfigFactory().load(LocaleConfiguration.class, path.toFile(), false);
                this.configurations.put(config.getLocale(), config);
                Locale[] clones = config.getClones();
                if (clones != null)
                {
                    for (Locale clone : clones)
                    {
                        this.configurations.put(clone, config);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            i18n.core.getLog().error(ex, "Failed to load language configurations!");
        }
    }

    @Override
    public Language loadLanguage(Locale locale)
    {
        if (this.hasConfiguration(locale))
        {
            LocaleConfiguration config = this.configurations.get(locale);

            if (config.getLocale().equals(locale))
            {
                // Main Locale of Configuration
                try
                {
                    return new NormalLanguage(config, i18n.getLoader().loadTranslations(locale), null); // TODO parent
                }
                catch (IOException e)
                {
                    i18n.core.getLog().error(e, "Failed to load language translations!");
                }
            }
            else
            {
                // Cloned Locale of Configuration -> Get main Language first
                Language mainLanguage = this.i18n.getLanguage(config.getLocale());
                if (mainLanguage != null)
                {
                    // Create Clone
                    return new ClonedLanguage(locale, mainLanguage);
                }
                // else couldnt load main language
            }
        }
        return null;
    }

    public boolean hasConfiguration(Locale locale)
    {
        return this.configurations.containsKey(locale);
    }
}
