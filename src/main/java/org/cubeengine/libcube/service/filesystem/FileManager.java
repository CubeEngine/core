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
package org.cubeengine.libcube.service.filesystem;

import static java.nio.file.Files.createSymbolicLink;

import com.google.inject.Inject;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.logscribe.Log;
import org.cubeengine.reflect.ReflectedFile;
import org.cubeengine.reflect.Reflector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

/**
 * Manages all the configurations of the CubeEngine.
 */
public class FileManager
{
    private final ModuleManager mm;
    private final File dataFolder;
    private final Reflector reflector;

    private final Path languagePath;
    private final Path logPath;

    private final Path translationPath;
    private final FileAttribute<?>[] folderCreateAttributes;

    @Inject
    public FileManager(ModuleManager moduleManager, File dataFolder, Reflector reflector)
    {
        this.mm = moduleManager;
        this.dataFolder = dataFolder;
        this.reflector = reflector;
        try
        {
            createSymbolicLink(Paths.get(System.getProperty("user.dir", "."), "CubeEngine"), dataFolder.toPath());
        }
        catch (IOException ignored)
        {}

        try
        {
            Path dataPath = dataFolder.toPath();
            if (Files.getFileAttributeView(dataPath.resolve("modules"), PosixFileAttributeView.class) != null)
            {
                folderCreateAttributes = new FileAttribute[] {PosixFilePermissions.asFileAttribute(FileUtil.DEFAULT_FOLDER_PERMS)};
                Files.createDirectories(dataPath);
                Files.setPosixFilePermissions(dataPath, FileUtil.DEFAULT_FOLDER_PERMS);
            }
            else
            {
                folderCreateAttributes = new FileAttribute[0];
            }

            this.languagePath = Files.createDirectories(dataPath.resolve("language"), folderCreateAttributes);
            this.logPath = Files.createDirectories(dataPath.resolve("log"), folderCreateAttributes);
            this.translationPath = Files.createDirectories(dataPath.resolve("translations"), folderCreateAttributes);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the data folder of the CubeEngine
     *
     * @return a file
     */
    public Path getDataPath()
    {
        return this.dataFolder.toPath();
    }

    /**
     * Returns the language directory
     *
     * @return the directory
     */
    public Path getLanguagePath()
    {
        return this.languagePath;
    }

    /**
     * Returns the log directory
     *
     * @return the directory
     */
    public Path getLogPath()
    {
        return this.logPath;
    }

    /**
     * Returns the translation override directory
     *
     * @return the directory
     */
    public Path getTranslationPath()
    {
        return translationPath;
    }

    public <T extends ReflectedFile<?, ?, ?>> T loadConfig(Object instance, Class<T> clazz)
    {
        T config = reflector.create(clazz);
        Path path = mm.getPathFor(instance.getClass());
        Log logger = mm.getLoggerFor(instance.getClass());
        config.setFile(path.resolve("config." + config.getCodec().getExtension()).toFile());
        if (config.reload(true))
        {
            logger.info("Saved new configuration file! config.{}", config.getCodec().getExtension());
        }
        return config;
    }

    public void injectConfig(Object instance, List<Field> fields)
    {
        for (Field field : fields)
        {
            ReflectedFile loaded = loadConfig(instance, (Class<? extends ReflectedFile>)field.getType());
            field.setAccessible(true);
            try
            {
                field.set(instance, loaded);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
