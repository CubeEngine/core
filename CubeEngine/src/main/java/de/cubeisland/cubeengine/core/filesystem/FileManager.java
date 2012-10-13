package de.cubeisland.cubeengine.core.filesystem;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;

/**
 * Manages all the configurations of the CubeEngine
 *
 * @author Phillip Schichtel
 */
public class FileManager
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private final File dataFolder;
    private final File languageDir;
    private final File logDir;
    private final File modulesDir;
    private ConcurrentMap<File, Resource> fileSources;

    public FileManager(File dataFolder) throws IOException
    {
        Validate.notNull(dataFolder, "The data folder must not be null!");
        if (!dataFolder.exists())
        {
            dataFolder.mkdirs();
        }
        else if (!dataFolder.isDirectory())
        {
            throw new IllegalArgumentException("The data folder was found, but it doesn't seem to be directoy!");
        }
        this.dataFolder = dataFolder;

        this.languageDir = new File(this.dataFolder, "language");
        if (!this.languageDir.isDirectory() && !this.languageDir.mkdirs())
        {
            throw new IOException("Failed to create the language folder");
        }

        this.logDir = new File(this.dataFolder, "log");
        if (!this.logDir.isDirectory() && !this.logDir.mkdirs())
        {
            throw new IOException("Failed to create the log folder");
        }

        this.modulesDir = new File(this.dataFolder, "modules");
        if (!this.modulesDir.isDirectory() && !this.modulesDir.mkdirs())
        {
            throw new IOException("Failed to create the modules folder");
        }
        
        this.fileSources = new ConcurrentHashMap<File, Resource>();
    }

    public File getDataFolder()
    {
        return this.dataFolder;
    }

    public File getLanguageDir()
    {
        return this.languageDir;
    }

    public File getLogDir()
    {
        return this.logDir;
    }

    public File getModulesDir()
    {
        return this.modulesDir;
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
    
    public InputStream getResourceStream(Resource resource)
    {
        if (resource == null)
        {
            return null;
        }
        return resource.getClass().getResourceAsStream(getSaneSource(resource));
    }

    public File getResourceFile(Resource resource)
    {
        Validate.notNull(resource, "The resource must not be null!");

        File file = this.dropResource(resource.getClass(), getSaneSource(resource), resource.getTarget(), false);
        this.fileSources.put(file, resource);
        return file;
    }

    public void dropResources(Resource[] resources)
    {
        Validate.notNull(resources, "The resources must not be null!");

        for (Resource resource : resources)
        {
            this.getResourceFile(resource);
        }
    }

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
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
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

    public InputStream getSourceOf(File file)
    {
        return this.getResourceStream(this.fileSources.get(file));
    }
}