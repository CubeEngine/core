package de.cubeisland.engine.core.i18n;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.i18n.DefinitionLoadingException;
import de.cubeisland.engine.i18n.LanguageLoader;
import de.cubeisland.engine.i18n.language.LanguageDefinition;

public class I18nLanguageLoader extends LanguageLoader
{
    private final Map<Locale, LocaleConfiguration> configurations = new HashMap<>();

    public I18nLanguageLoader(Core core)
    {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(core.getFileManager().getLanguagePath(), FileExtensionFilter.YAML))
        {
            for (Path path : directoryStream)
            {
                LocaleConfiguration config = core.getConfigFactory().load(LocaleConfiguration.class, path.toFile(), false);
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
            core.getLog().error(ex, "Failed to load language configurations!");
        }
    }

    @Override
    public LanguageDefinition loadDefinition(Locale locale) throws DefinitionLoadingException
    {
        return this.configurations.get(locale);
    }
}
