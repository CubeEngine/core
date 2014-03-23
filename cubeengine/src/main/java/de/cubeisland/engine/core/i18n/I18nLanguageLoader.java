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

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.filesystem.FileExtensionFilter;
import de.cubeisland.engine.i18n.language.DefinitionLoadingException;
import de.cubeisland.engine.i18n.language.LanguageDefinition;
import de.cubeisland.engine.i18n.language.LanguageLoader;

public class I18nLanguageLoader extends LanguageLoader
{
    private final Map<Locale, LocaleConfiguration> configurations = new HashMap<>();

    public I18nLanguageLoader(Core core)
    {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(core.getFileManager().getLanguagePath(), FileExtensionFilter.YAML))
        {
            // Search override Languages under CubeEngine/languages
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
            // Search provided Languages in CubeEngine.jar
            for (URL url : I18n.getFilesFromJar("languages/", ".yml", this.getClass()))
            {
                LocaleConfiguration config = core.getConfigFactory().load(LocaleConfiguration.class, url.openStream());
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
