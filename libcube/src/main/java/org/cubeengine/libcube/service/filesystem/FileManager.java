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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.createSymbolicLink;

import org.cubeengine.logscribe.Log;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.reflect.ReflectedFile;
import org.cubeengine.reflect.Reflector;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

/**
 * Manages all the configurations of the CubeEngine.
 */
public class FileManager
{
    private ModuleManager mm;
    private File dataFolder;
    private final Logger logger;
    private final Reflector reflector;

    private Path languagePath;
    private Path logPath;
    private Path tempPath;

    private Path translationPath;
    private ConcurrentMap<Path, Resource> fileSources;
    private FileAttribute<?>[] folderCreateAttributes;

    @Inject
    public FileManager(ModuleManager moduleManager, File dataFolder, Logger logger, Reflector reflector)
    {
        this.mm = moduleManager;
        this.dataFolder = dataFolder;
        this.logger = logger;
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
            this.tempPath = Files.createDirectories(dataPath.resolve("temp"), folderCreateAttributes);
            this.translationPath = Files.createDirectories(dataPath.resolve("translations"), folderCreateAttributes);
            this.fileSources = new ConcurrentHashMap<>();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        if (!FileUtil.hideFile(this.tempPath))
        {
            logger.info("Hiding the temp folder failed! This can be ignored!");
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

    private static String getSaneSource(Resource resource)
    {
        String source = resource.getSource();
        // we only accept absolute paths!
        if (!source.startsWith("/"))
        {
            source = "/" + source;
        }
        return source;
    }

    /**
     * Returns a resource as a stream
     *
     * @param resource the resource
     *
     * @return a stream to read from
     */
    public InputStream getResourceStream(Resource resource)
    {
        if (resource == null)
        {
            return null;
        }
        return resource.getClass().getResourceAsStream(getSaneSource(resource));
    }

    /**
     * Returns a resource as a file by first copying it to the file system
     *
     * @param resource the resource
     *
     * @return a file
     */
    public Path getResourceFile(Resource resource)
    {
        checkNotNull(resource, "The resource must not be null!");

        try
        {
            Path file = this.dropResource(resource.getClass(), getSaneSource(resource), getDataPath().resolve(resource.getTarget()), false);
            this.fileSources.put(file.toRealPath(), resource);
            return file;
        }
        catch (IOException e)
        {
            this.logger.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Drops an array of resources (usually the values of an enum)
     *
     * @param resources the resources
     */
    public void dropResources(Resource[] resources)
    {
        checkNotNull(resources, "The resources must not be null!");

        for (Resource resource : resources)
        {
            this.getResourceFile(resource);
        }
    }

    /**
     * Drops an resource
     *
     * @param clazz     the class of the resource
     * @param resPath   the resource path
     * @param file      the target file
     * @param overwrite whether to overwrite an existing file
     *
     * @return a file
     */
    public Path dropResource(Class clazz, String resPath, Path file, boolean overwrite) throws IOException
    {
        checkNotNull(clazz, "The class must not be null!");
        checkNotNull(resPath, "The resource path must not be null!");
        checkNotNull(file, "The file must not be null!");
        if (Files.exists(file) && !Files.isRegularFile(file))
        {
            throw new IOException("The given file exists, but is no file!");
        }
        if (Files.exists(file) && !overwrite)
        {
            return file; // return corresponding file
        }

        Files.createDirectories(file.getParent(), this.folderCreateAttributes);
        try (ReadableByteChannel sourceChannel = Channels.newChannel(clazz.getResourceAsStream(resPath)))
        {
            if (sourceChannel == null)
            {
                throw new FileNotFoundException("Could not find the resource '" + resPath + "'!");
            }
            else
            {
                try (FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
                {
                    FileUtil.copy(sourceChannel, fileChannel);
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return file;
    }

    /**
     * Revers look up for resources by file
     *
     * @param file the file
     *
     * @return stream of the resource
     */
    public InputStream getSourceOf(Path file)
    {
        try
        {
            file = file.toRealPath();
        }
        catch (IOException ignored)
        {}
        return this.getResourceStream(this.fileSources.get(file));
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

    public boolean copyModule(Path path)
    {
        try
        {
            Files.copy(path, tempPath.resolve(path.getFileName()));
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
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
