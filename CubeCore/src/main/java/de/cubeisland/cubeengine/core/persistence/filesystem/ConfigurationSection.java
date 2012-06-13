package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public class ConfigurationSection
{
    private Map<String, Object> values;
    
    public ConfigurationSection()
    {
        this.values = new HashMap<String, Object>();
    }
    
    public void set(String key, Object value)
    {
        if (key.contains("."))
        {
            this.createSection(key.substring(0, key.indexOf("."))).set(key.substring(key.indexOf(".")+1), value);
        }
        else
        {
            values.put(key, value);
        }
    }
    
    public Object get(String key)
    {
        if (key.contains("."))
        {
            String subkey = key.substring(key.indexOf(".")+1);
            ConfigurationSection section = (ConfigurationSection)this.get(key.substring(0, key.indexOf(".")));
            if (section == null) return null;
            return section.get(subkey);
        }
        else
        {
            return this.values.get(key);
        }
    }
    
    public ConfigurationSection getConfigurationSection(String path)
    {
        return (ConfigurationSection)this.get(path);
    }
    
    public ConfigurationSection createSection(String path)
    {
        ConfigurationSection section = new ConfigurationSection();
        this.values.put(path, section);
        return section;
    }

    public Map<String, Object> getValues()
    {
        return this.values;
    }

    public Iterable<String> getKeys()
    {
        return this.values.keySet();
    }
}
