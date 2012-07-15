package de.cubeisland.cubeengine.core.abstraction.implementations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author CodeInfection
 */
public class BukkitConfigration implements de.cubeisland.cubeengine.core.abstraction.Configuration
{
    private final File file;
    private final Configuration config;

    public BukkitConfigration(File file)
    {
        this(file, YamlConfiguration.loadConfiguration(file));
    }

    public BukkitConfigration(Configuration config)
    {
        this(null, config);
    }

    public BukkitConfigration(File file, Configuration config)
    {
        this.file = file;
        this.config = config;
    }

    public void set(String path, Object value)
    {
        this.config.set(path, value);
    }

    public Map<String, Object> getMap(String path)
    {
        return this.getMap(path, null);
    }

    private static Map<String, Object> mapFromSection(ConfigurationSection section)
    {
        if (section == null)
        {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Object value;

        for (String key : section.getKeys(false))
        {
            value = section.get(key);
            if (value instanceof ConfigurationSection)
            {
                value = mapFromSection((ConfigurationSection)value);
            }
        }

        return map;
    }

    public Map<String, Object> getMap(String path, Map<String, Object> def)
    {
        Map<String, Object> map = mapFromSection(this.config.getConfigurationSection(path));
        if (map == null)
        {
            return def;
        }
        else
        {
            return map;
        }
    }

    public <T> List<T> getList(String path)
    {
        return this.<T>getList(path, null);
    }

    public <T> List<T> getList(String path, List<T> def)
    {
        List<T> list = (List<T>)this.config.getList(path);
        if (list == null)
        {
            return def;
        }
        else
        {
            return list;
        }
    }

    public <T> T get(String path)
    {
        return this.<T>get(path, null);
    }

    public <T> T get(String path, T def)
    {
        T value = (T)this.config.get(path);
        if (value == null)
        {
            return def;
        }
        else
        {
            return value;
        }
    }

    public boolean load()
    {
        if (this.file != null)
        {
            try
            {
                FileConfiguration fileConfig;
                if (this.config instanceof FileConfiguration)
                {
                    fileConfig = (FileConfiguration)this.config;
                }
                else
                {
                    fileConfig = YamlConfiguration.loadConfiguration(this.file);
                    fileConfig.setDefaults(this.config);
                }
                fileConfig.load(this.file);
                return true;
            }
            catch (Exception e)
            {
            }
        }
        return false;
    }

    public boolean save()
    {
        if (file != null)
        {
            try
            {
                FileConfiguration fileConfig;
                if (this.config instanceof FileConfiguration)
                {
                    fileConfig = (FileConfiguration)this.config;
                }
                else
                {
                    fileConfig = YamlConfiguration.loadConfiguration(this.file);
                    fileConfig.setDefaults(this.config);
                }
                fileConfig.options().copyDefaults(true);
                fileConfig.save(this.file);
                return true;
            }
            catch (IOException e)
            {
            }
        }
        return false;
    }
}
