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
package de.cubeisland.engine.service.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.core.marker.Setup;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.contract.Contract;
import de.cubeisland.engine.reflect.ReflectedFile;
import de.cubeisland.engine.reflect.Reflector;
import org.slf4j.Logger;

import static java.nio.file.Files.createSymbolicLink;


/**
 * Manages all the configurations of the CubeEngine.
 */
@ServiceProvider(FileManager.class)
public class FileManager
{
    @Inject private Logger logger;
    @Inject private Reflector reflector;
    private File dataFolder;

    private Path languagePath;
    private Path logPath;
    private Path tempPath;

    private Path translationPath;
    private ConcurrentMap<Path, Resource> fileSources;
    private FileAttribute<?>[] folderCreateAttributes;

    @Inject
    public FileManager(File dataFolder)
    {
        this.dataFolder = dataFolder;
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
        Contract.expectNotNull(resource, "The resource must not be null!");

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
        Contract.expectNotNull(resources, "The resources must not be null!");

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
        Contract.expectNotNull(clazz, "The class must not be null!");
        Contract.expectNotNull(resPath, "The resource path must not be null!");
        Contract.expectNotNull(file, "The file must not be null!");
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

    public  <T extends ReflectedFile<?, ?, ?>> T  loadConfig(Module module, Class<T> clazz)
    {
        T config = reflector.create(clazz);
        config.setFile(module.getProvided(Path.class).resolve("config." + config.getCodec().getExtension()).toFile());
        if (config.reload(true))
        {
            module.getProvided(Log.class).info("Saved new configuration file! config.{}", config.getCodec().getExtension());
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
}
