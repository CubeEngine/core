package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Configuration for a CubeEngine module
 *
 * @author Phillip Schichtel
 */
public class CubeConfiguration extends YamlConfiguration
{
    private final File file;
    private static final String FILE_EXTENTION = ".yml";

    private CubeConfiguration(File file)
    {
        this.file = file;
    }

    public static CubeConfiguration get(File dir, String name, Configuration defaults)
    {
        if (dir == null)
        {
            throw new IllegalArgumentException("The directory must not be null!");
        }
        if (name == null)
        {
            throw new IllegalArgumentException("The name must not be null!");
        }
        CubeConfiguration config = new CubeConfiguration(new File(dir, name + FILE_EXTENTION));
        config.options().copyDefaults(true);

        if (defaults != null)
        {
            config.setDefaults(defaults);
        }

        return config;
    }

    public static CubeConfiguration get(File dir, Module module)
    {
        if (module == null)
        {
            throw new IllegalArgumentException("The module must not be null!");
        }
        
        dir.mkdirs();
        if (!dir.isDirectory())
        {
            throw new IllegalArgumentException("The directory must represent a directory!");
        }
        return get(dir, module.getModuleName(), module.getConfig().getDefaults());
    }

    public void load() throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        this.load(this.file);
    }

    public boolean safeLoad()
    {
        try
        {
            this.load();
            return true;
        }
        catch (Throwable t)
        {}
        return false;
    }


    public void save() throws IOException
    {
        this.save(this.file);
    }

    public boolean safeSave()
    {
        try
        {
            this.save();
            return false;
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
        return false;
    }
}
