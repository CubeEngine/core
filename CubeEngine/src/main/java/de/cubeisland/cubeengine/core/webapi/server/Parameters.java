package de.cubeisland.cubeengine.core.webapi.server;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class Parameters extends HashMap<String, String>
{
    public Parameters()
    {
        super();
    }

    public Parameters(Map<? extends String, ? extends String> m)
    {
        super(m);
    }

    public String getString(String key)
    {
        return this.getString(key, null);
    }

    public String getString(String key, String def)
    {
        String value = this.get(key);
        if (value == null)
        {
            return def;
        }
        return value;
    }

    public Integer getInt(String key)
    {
        return this.getInt(key, null);
    }

    public Integer getInt(String key, Integer def)
    {
        String value = this.get(key);
        if (value != null)
        {
            try
            {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {}
        }
        return def;
    }

    public Long getLong(String key)
    {
        return this.getLong(key, null);
    }

    public Long getLong(String key, Long def)
    {
        String value = this.get(key);
        if (value != null)
        {
            try
            {
                return Long.parseLong(value);
            }
            catch (NumberFormatException e)
            {}
        }
        return def;
    }

    public Double getDouble(String key)
    {
        return this.getDouble(key, null);
    }

    public Double getDouble(String key, Double def)
    {
        String value = this.get(key);
        if (value != null)
        {
            try
            {
                return Double.parseDouble(value);
            }
            catch (NumberFormatException e)
            {}
        }
        return def;
    }

    public Boolean getBoolean(String key)
    {
        return this.getBoolean(key, null);
    }

    public Boolean getBoolean(String key, Boolean def)
    {
        String value = this.get(key);
        if (value != null)
        {
            try
            {
                return Boolean.parseBoolean(value);
            }
            catch (NumberFormatException e)
            {}
        }
        return def;
    }
}