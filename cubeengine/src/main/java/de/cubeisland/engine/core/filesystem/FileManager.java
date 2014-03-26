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
package de.cubeisland.engine.core.filesystem;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil.RecursiveDirectoryDeleter;
import de.cubeisland.engine.core.util.Cleanable;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * Manages all the configurations of the CubeEngine.
 */
public class FileManager implements Cleanable
{
    private final Logger logger;
    private final Path dataPath;
    private final Path languagePath;
    private final Path logPath;
    private final Path modulesPath;
    private final Path tempPath;
    private final Path translationPath;

    private ConcurrentMap<Path, Resource> fileSources;
    private final FileAttribute<?>[] folderCreateAttributes;


    public FileManager(Logger logger, Path dataPath) throws IOException
    {
        expectNotNull(dataPath, "The CubeEngine plugin folder must not be null!");
        dataPath = dataPath.toAbsolutePath();

        this.logger = logger;

        this.dataPath = Files.createDirectories(dataPath).toRealPath();

        if (Files.getFileAttributeView(dataPath, PosixFileAttributeView.class) != null)
        {
            folderCreateAttributes = new FileAttribute[] {PosixFilePermissions.asFileAttribute(FileUtil.DEFAULT_FOLDER_PERMS)};

            Files.setPosixFilePermissions(this.dataPath, FileUtil.DEFAULT_FOLDER_PERMS);
        }
        else
        {
            folderCreateAttributes = new FileAttribute[0];
        }

        final Path linkSource = Paths.get(System.getProperty("user.dir", "."), CubeEngine.class.getSimpleName());

        this.languagePath = Files.createDirectories(dataPath.resolve("language"), folderCreateAttributes);

        this.logPath = Files.createDirectories(dataPath.resolve("log"), folderCreateAttributes);

        this.modulesPath = Files.createDirectories(dataPath.resolve("modules"), folderCreateAttributes);

        this.tempPath = Files.createDirectories(dataPath.resolve("temp"), folderCreateAttributes);

        this.translationPath = Files.createDirectories(dataPath.resolve("translations"), folderCreateAttributes);

        this.fileSources = new ConcurrentHashMap<>();

        try
        {
            Files.createSymbolicLink(linkSource, this.dataPath);
        }
        catch (IOException ignored)
        {}

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
        return this.dataPath;
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
     * Returns the modules directory
     *
     * @return the directory
     */
    public Path getModulesPath()
    {
        return this.modulesPath;
    }

    /**
     * Returns the temp directory
     *
     * @return the directory
     */
    public Path getTempPath()
    {
        return this.tempPath;
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

    public void clearTempDir()
    {
        logger.log(Level.INFO, "Clearing the temporary folder ''{0}''...", this.tempPath.toAbsolutePath());
        if (!Files.exists(this.tempPath))
        {
            return;
        }
        if (!Files.isDirectory(this.tempPath))
        {
            logger.log(Level.WARNING, "The path ''{0}'' is not a directory!", this.tempPath.toAbsolutePath());
            return;
        }

        try
        {
            Files.walkFileTree(this.tempPath, new RecursiveDirectoryDeleter());
        }
        catch (IOException e)
        {
            this.logger.log(Level.WARNING, "Failed to clear the temp directory!", e);
            return;
        }

        logger.info("Temporary folder cleared!");
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
        expectNotNull(resource, "The resource must not be null!");

        try
        {
            Path file = this.dropResource(resource.getClass(), getSaneSource(resource), this.dataPath.resolve(resource.getTarget()), false);
            this.fileSources.put(file.toRealPath(), resource);
            return file;
        }
        catch (IOException e)
        {
            this.logger.log(Level.WARNING, e.getLocalizedMessage(), e);
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
        expectNotNull(resources, "The resources must not be null!");

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
        expectNotNull(clazz, "The class must not be null!");
        expectNotNull(resPath, "The resource path must not be null!");
        expectNotNull(file, "The file must not be null!");
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
                    logger.log(Level.SEVERE, e.getMessage(), e);
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


    @Override
    public void clean()
    {
        this.clearTempDir();
    }
}
