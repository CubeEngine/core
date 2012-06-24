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
            this.createSection(key.substring(0, key.indexOf("."))).set(key.substring(key.indexOf(".") + 1), value);
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
            String subkey = key.substring(key.indexOf(".") + 1);
            ConfigurationSection section = (ConfigurationSection) this.get(key.substring(0, key.indexOf(".")));
            if (section == null)
            {
                return null;
            }
            return section.get(subkey);
        }
        else
        {
            return this.values.get(key);
        }
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        return (ConfigurationSection) this.get(path);
    }

    public ConfigurationSection createSection(String path)
    {

        if (path.contains("."))
        {
            ConfigurationSection section = this.createSection(path.substring(0, path.indexOf(".")));
            return section.createSection(path.substring(path.indexOf(".") + 1));
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
            this.createSection(path.substring(0, path.indexOf("."))).addComment(path.substring(path.indexOf(".") + 1), value);
        }
        else
        {
            this.comments.put(path, value);
        }
    }
}
