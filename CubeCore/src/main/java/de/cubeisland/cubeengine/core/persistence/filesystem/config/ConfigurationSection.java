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
    protected static final String COMMENT_PREFIX = "# ";
    
    private Map<String, Object> values;
    private Map<String, String> comments;

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

    private String toString(int offset)
    {
        String out = "";
        for (String key : this.getKeys())
        {
            Object value = this.get(key);
            out += this.getComment(key, offset);
            if (value == null)
            {
                System.out.println("Error while saving Key: \"" + key + "\" was null");
            }
            else if (value instanceof ConfigurationSection)
            {
                out += this.offset(offset) + key + ":\n";
                out += ((ConfigurationSection) value).toString(offset + 1);
            }
            else
            {
                out += this.offset(offset) + key + ": ";
                if (value instanceof String)
                {
                    out += "'" + value.toString() + "'";
                }
                else
                {
                    out += value.toString();
                }
                out += "\n";
            }
        }
        return out;
    }

    private String offset(int offset)
    {
        String off = "";
        for (int i = 0; i < offset; ++i)
        {
            off += "  ";
        }
        return off;
    }

    @Override
    public String toString()
    {
        return this.toString(0);
    }

    public String getComment(String path, int offset)
    {
        String comment = this.comments.get(path);
        if (comment == null)
        {
            return "";
        }
        else
        {
            comment = comment.replace("\n", "\n"+COMMENT_PREFIX);
            return "\n" + this.offset(offset) + COMMENT_PREFIX + comment + "\n";
        }
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
