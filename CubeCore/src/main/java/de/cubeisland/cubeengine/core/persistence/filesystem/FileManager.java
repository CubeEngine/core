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
    private Map<Module, CubeConfiguration> configs;
    private File dataFolder;
    private File moduleConfigDir;
    private File geoipFile;

    private CubeConfiguration databaseConfig;
    private CubeConfiguration coreConfig;

    public FileManager(CubeCore core)
    {
        this.core = core;
        this.configs = new THashMap<Module, CubeConfiguration>();
        this.dataFolder = new File(core.getDataFolder().getParentFile(), "CubeEngine");
        this.dataFolder.mkdirs();

        this.moduleConfigDir = new File(this.dataFolder, "modules");
        this.moduleConfigDir.mkdirs();

        this.geoipFile = new File(this.dataFolder, "GeoIP.dat");
    }

    public File getDataFolder()
    {
        return this.dataFolder;
    }

    public File getModuleConfigDir()
    {
        return this.moduleConfigDir;
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

    public CubeConfiguration getModuleConfig(Module module)
    {
        CubeConfiguration config = this.configs.get(module);
        if (config == null)
        {
            config = CubeConfiguration.get(this.moduleConfigDir, module);
            this.configs.put(module, config);
        }

        return config;
    }
    public File getResource(Class clazz, String path)
    {
        if (clazz == null)
        {
            throw new IllegalArgumentException("The class must not be null!");
        }
        if (path == null)
        {
            throw new IllegalArgumentException("The path must not be null!");
        }
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        File target = new File(this.dataFolder, path);
        if (!target.exists())
        {
            InputStream reader = this.getClass().getResourceAsStream("/resources/" + path);
            if (reader != null)
            {
                try
                {
                    OutputStream writer = new FileOutputStream(target);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) > 0)
                    {
                        writer.write(buffer, 0, bytesRead);
                    }
                    writer.flush();
                    writer.close();
                    reader.close();
                    return target;
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
        else
        {
            return target;
        }

        return null;
    }

    public void clean()
    {
        this.configs.clear();
        this.coreConfig = null;
        this.databaseConfig = null;
    }
}
