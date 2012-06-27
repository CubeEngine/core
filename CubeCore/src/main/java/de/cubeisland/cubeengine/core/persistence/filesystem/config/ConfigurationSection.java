package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import gnu.trove.map.hash.THashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public class ConfigurationSection
{
    private Map<String, Object> values;
    private Map<String, String> comments;

    public ConfigurationSection()
    {
        this.values = new LinkedHashMap<String, Object>();
        this.comments = new THashMap<String, String>();
    }

    /**
     * Sets a value for the specified Key
     *
     * @param key the key
     * @param value the value to set
     */
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

    /**
     * Gets the value saved under the key
     *
     * @param key the key
     * @return the value or null if no value saved
     */
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

    /**
     * Splits up the Key and returns the BaseKey
     *
     * @param key the key
     * @return the BaseKey
     */
    public String getBaseKey(String key)
    {
        return key.substring(0, key.indexOf("."));
    }

    /**
     * Splits up the Key and returns the SubKey
     *
     * @param key the key
     * @return the SubKey
     */
    public String getSubKey(String key)
    {
        return key.substring(key.indexOf(".") + 1);
    }

    /**
     * Gets the section at path
     *
     * @param path the path of the section
     * @return the section
     */
    public ConfigurationSection getSection(String path)
    {
        return (ConfigurationSection)this.get(path);
    }

    /**
     * Gets or create the section at path
     *
     * @param path the path of the section
     * @return the section
     */
    public ConfigurationSection createSection(String path)
    {
        if (path.contains("."))
        {
            ConfigurationSection section = this.createSection(this.getBaseKey(path));
            return section.createSection(this.getSubKey(path));
        }
        else
        {
            ConfigurationSection section = this.getSection(path);
            if (section == null)
            {
                section = new ConfigurationSection();
                this.values.put(path, section);
            }
            return section;
        }
    }

    /**
     * Gets all values saved in this Section
     *
     * @return the values
     */
    public Map<String, Object> getValues()
    {
        return this.values;
    }

    /**
     * Gets all the keys used in this Section
     *
     * @return the keys
     */
    public Iterable<String> getKeys()
    {
        return this.values.keySet();
    }

    /**
     * Adds a Comment to the specified key
     *
     * @param key the key
     * @param comment the comment
     */
    public void addComment(String key, String comment)
    {
        if (key.contains("."))
        {
            this.createSection(this.getBaseKey(key)).addComment(this.getSubKey(key), comment);
        }
        else
        {
            this.comments.put(key, comment);
        }
    }

    /**
     * Gets all the Comments saved in this Section
     *
     * @return the comments
     */
    public Map<String, String> getComments()
    {
        return this.comments;
    }
}
