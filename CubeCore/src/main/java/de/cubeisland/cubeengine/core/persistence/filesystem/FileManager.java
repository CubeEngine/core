package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.modules.CubeModule;
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
    private Map<CubeModule, CubeConfiguration> configs;
    private File configBaseDir;
    private File moduleConfigDir;
    private File geoipFile;

    private CubeConfiguration databaseConfig;
    private CubeConfiguration coreConfig;

    public FileManager(CubeCore core)
    {
        this.core = core;
        this.configs = new THashMap<CubeModule, CubeConfiguration>();
        this.configBaseDir = new File(core.getDataFolder().getParentFile(), "CubeEngine");
        this.configBaseDir.mkdirs();

        this.moduleConfigDir = new File(this.configBaseDir, "modules");
        this.moduleConfigDir.mkdirs();

        this.geoipFile = new File(this.configBaseDir, "GeoIP.dat");
    }

    public File getConfigDir()
    {
        return this.configBaseDir;
    }

    public File getModuleConfigDir()
    {
        return this.moduleConfigDir;
    }

    public File getGeoipFile()
    {
        if (!this.geoipFile.exists())
        {
            InputStream reader = this.getClass().getResourceAsStream("/resources/GeoIP.dat");
            if (reader != null)
            {
                try
                {
                    OutputStream writer = new FileOutputStream(this.geoipFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = reader.read(buffer)) > 0)
                    {
                        writer.write(buffer);
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

        return this.geoipFile;
    }

    public CubeConfiguration getCoreConfig()
    {
        if (this.coreConfig == null)
        {
            this.coreConfig = CubeConfiguration.get(this.configBaseDir, "core", this.core.getConfig().getDefaults());
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

            this.databaseConfig = CubeConfiguration.get(this.configBaseDir, "database", defaults);
            this.databaseConfig.safeLoad();
        }
        return this.databaseConfig;
    }

    public CubeConfiguration getModuleConfig(CubeModule module)
    {
        CubeConfiguration config = this.configs.get(module);
        if (config == null)
        {
            config = CubeConfiguration.get(this.moduleConfigDir, module);
            this.configs.put(module, config);
        }

        return config;
    }

    public void clean()
    {
        this.configs.clear();
        this.coreConfig = null;
        this.databaseConfig = null;
    }
}
