package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public class ConfigurationSection
{
    protected Map<String, Object> values;
    protected Map<String, String> comments;

    public ConfigurationSection()
    {
        this.values = new LinkedHashMap<String, Object>();
        this.comments = new HashMap<String, String>();
    }

    public void set(String key, Object value)
    {
        if (key.contains("."))
        {
            this.createSection(this.getBaseKey(key)).set(this.getSubKey(key), value);
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
            ConfigurationSection section = (ConfigurationSection)this.get(this.getBaseKey(key));
            if (section == null)
            {
                return null;
            }
            return section.get(this.getSubKey(key));
        }
        else
        {
            return this.values.get(key);
        }
    }
    
    public String getBaseKey(String key)
    {
        return key.substring(0, key.indexOf("."));
    }
    
    public String getSubKey(String key)
    {
        return key.substring(key.indexOf(".") + 1);
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        return (ConfigurationSection)this.get(path);
    }

    public ConfigurationSection createSection(String path)
    {
        if (path.contains("."))
        {
            ConfigurationSection section = this.createSection(this.getBaseKey(path));
            return section.createSection(this.getSubKey(path));
        }
        else
        {
            ConfigurationSection section = this.getConfigurationSection(path);
            if (section == null)
            {
                section = new ConfigurationSection();
                this.values.put(path, section);
            }
            return section;
        }
    }

    public Map<String, Object> getValues()
    {
        return this.values;
    }

    public Iterable<String> getKeys()
    {
        return this.values.keySet();
    }

    public void addComment(String path, String value)
    {
        if (path.contains("."))
        {
            this.createSection(this.getBaseKey(path)).addComment(this.getSubKey(path), value);
        }
        else
        {
            this.comments.put(path, value);
        }
    }
}
