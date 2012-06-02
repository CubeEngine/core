package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Manages all the configurations of the CubeEngine
 *
 * @author Phillip Schichtel
 */
public class FileManager
{
    private CubeCore core;
    private Map<Module, File> configsDirs;
    private File dataFolder;
    private File configDir;
    private File languageDir;

    public FileManager(CubeCore core, File pluginsFolder)
    {
        this.core = core;
        this.configsDirs = new THashMap<Module, File>();
        this.dataFolder = new File(pluginsFolder, "CubeEngine");
        this.dataFolder.mkdirs();

        this.configDir = new File(this.dataFolder, "config");
        this.configDir.mkdirs();
    }

    public File getDataFolder()
    {
        return this.dataFolder;
    }

    public File getConfigDir()
    {
        return this.configDir;
    }

    public File getLanguageDir()
    {
        return this.languageDir;
    }

    public File getResourceFile(Resource resource)
    {
        if (resource == null)
        {
            throw new IllegalArgumentException("The resource must not be null!");
        }
        String source = resource.getSource();
        String target = resource.getTarget();

        if (source.startsWith("/"))
        {
            source = source.substring(1);
        }
        if (target.startsWith("/"))
        {
            target = target.substring(1);
        }

        File targetFile = new File(this.dataFolder, target);
        this.dropResource(resource.getClass(), source, targetFile, false);

        return targetFile;
    }

    public void dropResource(Class clazz, String resPath, String filePath, boolean overwrite)
    {
        if (filePath == null)
        {
            throw new IllegalArgumentException("The file path must not be null!");
        }

        if (filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }
        this.dropResource(clazz, resPath, new File(this.dataFolder, filePath), overwrite);
    }

    public void dropResource(Class clazz, String resPath, File file, boolean overwrite)
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
        if (file.exists() && !overwrite)
        {
            return;
        }

        InputStream reader = this.getClass().getResourceAsStream(resPath);
        if (reader != null)
        {
            try
            {
                OutputStream writer = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
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
    }

    public void clean()
    {
        //TODO noch nötig??
    }
}
