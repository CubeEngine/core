package de.cubeisland.cubeengine.core.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Manages all the configurations of the CubeEngine
 *
 * @author Phillip Schichtel
 */
public class FileManager
{
    private final File dataFolder;
    private final File languageDir;
    private final File logDir;
    private final File modulesDir;

    public FileManager(File pluginsFolder) throws IOException
    {
        this.dataFolder = new File(pluginsFolder, "CubeEngine");
        if (!this.dataFolder.isDirectory() && !this.dataFolder.mkdirs())
        {
            throw new IOException("Failed to create the data folder");
        }

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

    public File getResourceFile(Resource resource)
    {
        if (resource == null)
        {
            throw new IllegalArgumentException("The resource must not be null!");
        }
        String source = resource.getSource();

        // we only accept absolute paths!
        if (!source.startsWith("/"))
        {
            source = "/" + source;
        }

        return this.dropResource(resource.getClass(), source, resource.getTarget(), false);
    }

    public void dropResources(Resource[] resources)
    {
        if (resources == null)
        {
            throw new IllegalArgumentException("The resources must not be null!");
        }
        for (Resource resource : resources)
        {
            this.getResourceFile(resource);
        }
    }

    public File dropResource(Class clazz, String resPath, String filePath, boolean overwrite)
    {
        if (filePath == null)
        {
            throw new IllegalArgumentException("The file path must not be null!");
        }
        if (resPath == null)
        {
            throw new IllegalArgumentException("The resource path must not be null!");
        }

        if (filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }
        return this.dropResource(clazz, resPath, new File(this.dataFolder, filePath.replace('\\', File.separatorChar).replace('/', File.separatorChar)), overwrite);
    }

    public File dropResource(Class clazz, String resPath, File file, boolean overwrite)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("The class must not be null!");
        }
        if (resPath == null)
        {
            throw new IllegalArgumentException("The resource path must not be null!");
        }
        if (file == null)
        {
            throw new IllegalArgumentException("The file must not be null!");
        }
        if (file.exists() && !file.isFile())
        {
            throw new IllegalArgumentException("The given file exists, but is no file!");
        }
        if (file.exists() && !overwrite)
        {
            return file;
        }
        InputStream reader = clazz.getResourceAsStream(resPath);//TODO ModuleClassLoader does not take the Resource from the module but from Core
        if (reader != null)
        {
            try
            {
                file.getParentFile().mkdirs();
                OutputStream writer = new FileOutputStream(file);
                final byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = reader.read(buffer)) > 0)
                {
                    writer.write(buffer, 0, bytesRead);
                }
                writer.flush();
                writer.close();
                reader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
        else
        {
            throw new RuntimeException("Could not find the resource '" + resPath + "'!");
        }

        return file;
    }
}