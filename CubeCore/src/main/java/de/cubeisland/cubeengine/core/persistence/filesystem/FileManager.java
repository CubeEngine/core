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
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;

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

    private CubeConfiguration databaseConfig;
    private CubeConfiguration coreConfig;
    
    private static final String FILE_EXTENTION = ".yml";

    public FileManager(CubeCore core)
    {
        this.core = core;
        this.configsDirs = new THashMap<Module, File>();
        this.dataFolder = new File(core.getDataFolder().getParentFile(), "CubeEngine");
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

    public CubeConfiguration getDatabaseConfig()
    {
        if (databaseConfig == null)
        {
            Configuration defaults = new MemoryConfiguration();
            defaults.set("mysql.host", "localhost");
            defaults.set("mysql.port", 3306);
            defaults.set("mysql.user", "minecraft");
            defaults.set("mysql.password", "12345678");
            defaults.set("mysql.database", "minecraft");
            defaults.set("mysql.tableprefix", "cube_");

            this.databaseConfig = CubeConfiguration.get(this.dataFolder, "database", defaults);
            this.databaseConfig.safeLoad();
        }
        return this.databaseConfig;
    }

    public CubeConfiguration getCoreConfig()
    {
        if (this.coreConfig == null)
        {
            this.coreConfig = CubeConfiguration.get(this.dataFolder, "core", this.core.getConfig().getDefaults());
            this.coreConfig.safeLoad();
        }

        return this.coreConfig;
    }

    public File getModuleConfig(Module module)
    {
        File file = this.configsDirs.get(module);
        if (file == null)
        {
            file = new File(this.configDir, module.getModuleName() + FILE_EXTENTION);
            this.configsDirs.put(module, file);
        }

        return file;
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
        this.configsDirs.clear();
        this.coreConfig = null;
        this.databaseConfig = null;
    }
}
