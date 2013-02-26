package de.cubeisland.cubeengine.core.filesystem;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.Cleanable;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.CubeEngine.runsOnWindows;
import static de.cubeisland.cubeengine.core.logger.LogLevel.*;

/**
 * Manages all the configurations of the CubeEngine.
 */
public class FileManager implements Cleanable
{
    private final Logger logger;
    private final File dataFolder;
    private final File languageDir;
    private final File logDir;
    private final File modulesDir;
    private final File tempDir;
    private ConcurrentMap<File, Resource> fileSources;

    public FileManager(Core core, File dataFolder) throws IOException
    {
        this.logger = core.getCoreLogger();
        Validate.notNull(dataFolder, "The CubeEngine plugin folder must not be null!");
        if (!dataFolder.exists())
        {
            if (!dataFolder.mkdirs())
            {
                throw new IOException("The CubeEngine plugin folder could not be created: " + dataFolder.getAbsolutePath());
            }
            dataFolder.setWritable(true, true);
        }
        else if (!dataFolder.isDirectory())
        {
            throw new IOException("The CubeEngine plugin folder was found, but it doesn't seem to be directory: " + dataFolder.getAbsolutePath());
        }
        if (!dataFolder.canWrite() && !dataFolder.setWritable(true, true))
        {
            throw new IOException("The CubeEngine plugin folder is not writable: " + dataFolder.getAbsolutePath());
        }
        this.dataFolder = dataFolder;

        this.languageDir = new File(this.dataFolder, "language");
        if (!this.languageDir.isDirectory() && !this.languageDir.mkdirs())
        {
            throw new IOException("Failed to create the language folder: " + this.languageDir.getAbsolutePath());
        }
        if (!this.languageDir.canWrite() && !this.languageDir.setWritable(true, true))
        {
            throw new IOException("The language folder is not writable!");
        }

        this.logDir = new File(this.dataFolder, "log");
        if (!this.logDir.isDirectory() && !this.logDir.mkdirs())
        {
            throw new IOException("Failed to create the log folder: " + this.logDir.getAbsolutePath());
        }
        if (!this.logDir.canWrite() && !this.logDir.setWritable(true, true))
        {
            throw new IOException("The log folder is not writable!: " + this.logDir.getAbsolutePath());
        }

        this.modulesDir = new File(this.dataFolder, "modules");
        if (!this.modulesDir.isDirectory() && !this.modulesDir.mkdirs())
        {
            throw new IOException("Failed to create the modules folder: " + this.modulesDir.getAbsolutePath());
        }
        if (!this.modulesDir.canWrite() && !this.modulesDir.setWritable(true, true))
        {
            throw new IOException("The modules folder is not writable: " + this.modulesDir.getAbsolutePath());
        }

        final File linkSource = new File(System.getProperty("user.dir", "."), "modules");
        if (!isSymLink(linkSource) && !createSymLink(linkSource, this.modulesDir))
        {
            logger.log(NOTICE, "Linking to the modules directory failed! This can be ignored.");
        }

        this.tempDir = new File(this.dataFolder, "temp");
        if (!this.tempDir.isDirectory() && !this.tempDir.mkdirs())
        {
            throw new IOException("Failed to create the temp folder: " + this.tempDir.getAbsolutePath());
        }
        if (!this.tempDir.canWrite() && !this.tempDir.setWritable(true, true))
        {
            throw new IOException("The temp folder is not writable: " + this.tempDir.getAbsolutePath());
        }
        if (!hideFile(this.tempDir))
        {
            logger.log(NOTICE, "Hiding the temp folder failed! This can be ignored!");
        }

        this.fileSources = new ConcurrentHashMap<File, Resource>();
    }

    public static boolean hideFile(File file)
    {
        if (runsOnWindows())
        {
            try
            {
                return Runtime.getRuntime().exec(new String[] {
                "attrib", "+H", file.getAbsolutePath()
                }).waitFor() == 0;
            }
            catch (Exception e)
            {}
        }
        return false;
    }

    public static boolean createSymLink(File source, File target)
    {
        final String[] command;
        if (runsOnWindows())
        {
            if (target.isDirectory())
            {
                command = new String[] {
                "cmd", "/c", "mklink", "/d", source.getAbsolutePath(), target.getAbsolutePath()
                };
            }
            else
            {
                command = new String[] {
                "cmd", "/c", "mklink", source.getAbsolutePath(), target.getAbsolutePath()
                };
            }
        }
        else
        {
            command = new String[] {
            "ln", "-s", target.getAbsolutePath(), source.getAbsolutePath()
            };
        }
        try
        {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        }
        catch (Exception e)
        {}
        return false;
    }

    public static boolean isSymLink(File file) throws IOException
    {
        final File canon;
        if (file.getParent() == null)
        {
            canon = file;
        }
        else
        {
            canon = new File(file.getParentFile().getCanonicalFile(), file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    /**
     * Returns the data folder of the CubeEngine
     *
     * @return a file
     */
    public File getDataFolder()
    {
        return this.dataFolder;
    }

    /**
     * Returns the language directory
     *
     * @return the directory
     */
    public File getLanguageDir()
    {
        return this.languageDir;
    }

    /**
     * Returns the log directory
     *
     * @return the directory
     */
    public File getLogDir()
    {
        return this.logDir;
    }

    /**
     * Returns the modules directory
     *
     * @return the directory
     */
    public File getModulesDir()
    {
        return this.modulesDir;
    }

    /**
     * Returns the modules directory
     *
     * @return the directory
     */
    public File getTempDir()
    {
        return this.tempDir;
    }

    public void clearTempDir()
    {
        logger.log(INFO, "Clearing the temporary folder ''{0}''...", this.tempDir.getAbsolutePath());
        for (File file : this.tempDir.listFiles())
        {
            try
            {
                deleteRecursive(file);
            }
            catch (IOException e)
            {
                logger.log(NOTICE, "Failed to remove the file ''{0}''", file.getAbsolutePath());
            }
        }
        logger.log(INFO, "Temporary folder cleared!");
    }

    public static void deleteRecursive(File file) throws IOException
    {
        if (file == null)
        {
            return;
        }
        if (file.isDirectory())
        {
            for (File f : file.listFiles())
            {
                try
                {
                    deleteRecursive(f);
                }
                catch (FileNotFoundException ignored)
                {}
            }
        }
        if (!file.delete())
        {
            throw new IOException("File to delete the file '" + file.getAbsolutePath() + "'");
        }
    }

    public static boolean copyFile(File source, File target) throws FileNotFoundException
    {
        final InputStream is = new FileInputStream(source);
        final OutputStream os = new FileOutputStream(target);

        try
        {
            copyFile(is, os);
            return true;
        }
        catch (IOException ignored)
        {}
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                CubeEngine.getLogger().log(WARNING, "Failed to close a file stream!", e);
            }

            try
            {
                os.close();
            }
            catch (IOException e)
            {
                CubeEngine.getLogger().log(WARNING, "Failed to close a file stream!", e);
            }
        }
        return false;
    }

    public static void copyFile(InputStream is, OutputStream os) throws IOException
    {
        final byte[] buffer = new byte[1024 * 4];
        int bytesRead;

        while ((bytesRead = is.read(buffer, 0, buffer.length)) > 0)
        {
            os.write(buffer, 0, bytesRead);
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
     * @return a file
     */
    public File getResourceFile(Resource resource)
    {
        Validate.notNull(resource, "The resource must not be null!");

        File file = this.dropResource(resource.getClass(), getSaneSource(resource), resource.getTarget(), false);
        this.fileSources.put(file, resource);
        return file;
    }

    /**
     * Drops an array of resources (usually the values of an enum)
     *
     * @param resources the resources
     */
    public void dropResources(Resource[] resources)
    {
        Validate.notNull(resources, "The resources must not be null!");

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
     * @param filePath  the target file path
     * @param overwrite wheter to overwrite an existing file
     * @return a file
     */
    public File dropResource(Class clazz, String resPath, String filePath, boolean overwrite)
    {
        Validate.notNull(filePath, "The file path must not be null!");
        Validate.notNull(resPath, "The resource path must not be null!");

        if (filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }
        return this.dropResource(clazz, resPath, new File(this.dataFolder, filePath.replace('\\', File.separatorChar).replace('/', File.separatorChar)), overwrite);
    }

    /**
     * Drops an resource
     *
     * @param clazz     the class of the resource
     * @param resPath   the resource path
     * @param file      the target file
     * @param overwrite whether to overwrite an existing file
     * @return a file
     */
    public File dropResource(Class clazz, String resPath, File file, boolean overwrite)
    {
        Validate.notNull(clazz, "The class must not be null!");
        Validate.notNull(resPath, "The resource path must not be null!");
        Validate.notNull(file, "The file must not be null!");
        if (file.exists() && !file.isFile())
        {
            throw new IllegalArgumentException("The given file exists, but is no file!");
        }
        if (file.exists() && !overwrite)
        {
            return file;
        }
        InputStream reader = clazz.getResourceAsStream(resPath);
        if (reader != null)
        {
            OutputStream writer = null;
            try
            {
                file.getParentFile().mkdirs();
                writer = new FileOutputStream(file);
                final byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) > 0)
                {
                    writer.write(buffer, 0, bytesRead);
                }
                writer.flush();
            }
            catch (IOException e)
            {
                logger.log(LogLevel.ERROR, e.getMessage(), e);
            }
            finally
            {
                try
                {
                    reader.close();
                }
                catch (IOException ignored)
                {}
                if (writer != null)
                {
                    try
                    {
                        writer.close();
                    }
                    catch (IOException ignored)
                    {}
                }
            }
        }
        else
        {
            throw new RuntimeException("Could not find the resource '" + resPath + "'!");
        }
        return file;
    }

    /**
     * Revers look up for resources by file
     *
     * @param file the file
     * @return stream of the resource
     */
    public InputStream getSourceOf(File file)
    {
        return this.getResourceStream(this.fileSources.get(file));
    }

    @Override
    public void clean()
    {
        this.clearTempDir();
    }
}
