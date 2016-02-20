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
package org.cubeengine.service.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import de.cubeisland.engine.i18n.language.DefinitionLoadingException;
import de.cubeisland.engine.i18n.language.LanguageDefinition;
import de.cubeisland.engine.i18n.language.LanguageLoader;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.core.ModularityClassLoader;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.module.core.CoreModule;
import org.cubeengine.service.filesystem.FileManager;

import static org.cubeengine.service.filesystem.FileExtensionFilter.YAML;

public class I18nLanguageLoader extends LanguageLoader
{
    private final Map<Locale, LocaleConfiguration> configurations = new HashMap<>();
    private final Reflector reflector;
    private Log log;

    public I18nLanguageLoader(Reflector reflector, FileManager fm, Log log)
    {
        this.reflector = reflector;
        this.log = log;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fm.getLanguagePath(), YAML))
        {
            // Search override Languages under CubeEngine/languages
            for (Path path : directoryStream)
            {
                LocaleConfiguration config = reflector.load(LocaleConfiguration.class, path.toFile(), false);
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
            log.error(ex, "Failed to load language configurations!");
        }

    }

    public void provideLanguages(CoreModule core)
    {
        try
        {
            // Search provided Languages in CoreModule.jar
            ModularityClassLoader classLoader = core.getInformation().getClassLoader();
            if (classLoader == null)
            {
                core.getLog().warn("ModularityClassLoader for CoreModule not set!");
                return; // No classLoader => Core was injected into classpath
            }
            loadLanguages(I18n.getFilesFromURL("languages/", ".yml", classLoader, classLoader.getSourceURL()));
        }
        catch (IOException ex)
        {
            log.error(ex, "Failed to load language configurations!");
        }
    }

    protected void loadLanguages(List<URL> languageFiles) throws IOException
    {
        for (URL url : languageFiles)
        {
            try (Reader reader = new InputStreamReader(url.openStream()))
            {
                LocaleConfiguration config = reflector.load(LocaleConfiguration.class, reader);
                if (!this.configurations.containsKey(config.getLocale()))
                {
                    this.configurations.put(config.getLocale(), config);
                }
                Locale[] clones = config.getClones();
                if (clones != null)
                {
                    for (Locale clone : clones)
                    {
                        if (!this.configurations.containsKey(clone))
                        {
                            this.configurations.put(clone, config);
                        }
                    }
                }
            }
        }
    }

    @Override
    public LanguageDefinition loadDefinition(Locale locale) throws DefinitionLoadingException
    {
        return this.configurations.get(locale);
    }
}
