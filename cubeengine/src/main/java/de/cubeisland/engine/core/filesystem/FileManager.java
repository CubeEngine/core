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
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.Cleanable;
import org.slf4j.Logger;

import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.nio.file.attribute.PosixFilePermissions.fromString;

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
    private ConcurrentMap<Path, Resource> fileSources;

    private static FileAttribute<Set<PosixFilePermission>> FILE_PERMISSIONS = asFileAttribute(fromString("rwxrwxr-x"));

    public FileManager(Core core, Path dataPath) throws IOException
    {
        assert dataPath != null : "The CubeEngine plugin folder must not be null!";
        dataPath = dataPath.toAbsolutePath().toRealPath();

        this.logger = core.getLog();

        this.dataPath = Files.createDirectories(dataPath, FILE_PERMISSIONS);

        final Path linkSource = Paths.get(System.getProperty("user.dir", "."), CubeEngine.class.getSimpleName());

        this.languagePath = Files.createDirectories(dataPath.resolve("language"), FILE_PERMISSIONS);

        this.logPath = Files.createDirectories(dataPath.resolve("log"), FILE_PERMISSIONS);

        this.modulesPath = Files.createDirectories(dataPath.resolve("modules"), FILE_PERMISSIONS);

        this.tempPath = Files.createDirectories(dataPath.resolve("language"), FILE_PERMISSIONS);

        this.fileSources = new ConcurrentHashMap<>();

        try
        {
            Files.createSymbolicLink(linkSource, this.dataPath);
        }
        catch (IOException ignored)
        {}

        if (!hideFile(this.tempPath))
        {
            logger.info("Hiding the temp folder failed! This can be ignored!");
        }
    }

    public static boolean hideFile(Path path)
    {
        try
        {
            DosFileAttributeView attributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
            attributeView.setHidden(true);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static final Set<PosixFilePermission> READ_ONLY_PERMISSION = PosixFilePermissions.fromString("--r--r---");

    public static boolean setReadOnly(Path file)
    {
        try
        {
            Files.getFileAttributeView(file, PosixFileAttributeView.class).setPermissions(READ_ONLY_PERMISSION);
        }
        catch (Exception ignore)
        {
            try
            {
                Files.getFileAttributeView(file, DosFileAttributeView.class).setReadOnly(true);
            }
            catch (Exception  ignored)
            {
                return false;
            }
        }
        return true;
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
     * Returns the modules directory
     *
     * @return the directory
     */
    public Path getTempPath()
    {
        return this.tempPath;
    }

    public void clearTempDir()
    {
        logger.debug("Clearing the temporary folder '{}'...", this.tempPath.toAbsolutePath());
        if (!Files.exists(this.tempPath))
        {
            return;
        }
        if (!Files.isDirectory(this.tempPath))
        {
            logger.warn("The path '{}' is not a directory!", this.tempPath.toAbsolutePath());
            return;
        }

        try
        {
            Files.walkFileTree(this.tempPath, new RecursiveDirectoryDeleter());
        }
        catch (IOException e)
        {
            this.logger.warn("Failed to clear the temp directory!", e);
            return;
        }

        logger.debug("Temporary folder cleared!");
    }

    private static final RecursiveDirectoryDeleter TREE_WALKER = new RecursiveDirectoryDeleter();

    public static void deleteRecursive(Path file) throws IOException
    {
        if (file == null)
        {
            return;
        }
        if (Files.isRegularFile(file))
        {
            Files.delete(file);
        }
        else
        {
            Files.walkFileTree(file, TREE_WALKER);
        }
    }

    public static void copy(ReadableByteChannel in, WritableByteChannel out) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 4);
        int bytesRead;


        while (in.read(buffer) >= 0 || buffer.position() > 0)
        {
            buffer.flip();
            out.write(buffer);
            buffer.compact();
        }
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
        assert resource != null : "The resource must not be null!";

        try
        {
            Path file = this.dropResource(resource.getClass(), getSaneSource(resource), this.dataPath.resolve(resource.getTarget()), false);
            this.fileSources.put(file.toRealPath(), resource);
            return file;
        }
        catch (IOException e)
        {
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
        assert resources != null : "The resources must not be null!";

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
        assert clazz != null : "The class must not be null!";
        assert resPath != null : "The resource path must not be null!";
        assert file != null : "The file must not be null!";
        if (Files.exists(file) && !Files.isRegularFile(file))
        {
            throw new IOException("The given file exists, but is no file!");
        }
        if (Files.exists(file) && !overwrite)
        {
            throw new FileAlreadyExistsException(file.toString());
        }

        Files.createDirectories(file.getParent());
        try (ReadableByteChannel sourceChannel = Channels.newChannel(clazz.getResourceAsStream(resPath)))
        {
            if (sourceChannel == null)
            {
                throw new FileNotFoundException("Could not find the resource '" + resPath + "'!");
            }
            else
            {
                try (FileChannel fileChannel = FileChannel.open(file))
                {
                    FileManager.copy(sourceChannel, fileChannel);
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


    @Override
    public void clean()
    {
        this.clearTempDir();
    }

    public static class RecursiveDirectoryDeleter extends SimpleFileVisitor<Path>
    {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
        {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            if (exc != null)
            {
                throw exc;
            }
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
