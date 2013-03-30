package de.cubeisland.cubeengine.core.webapi;

import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.Map;

public class Parameters
{
    private final Map<String, List<String>> data;

    public Parameters(Map<String, List<String>> data)
    {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, int index, Class<T> type)
    {
        List<String> values = this.data.get(name);
        if (values == null)
        {
            return null;
        }
        String value = values.get(index);
        if (type != String.class)
        {
            //return ArgumentReader.read(type, value).getRight(); TODO BROKEN
        }
        return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, int index, T def)
    {
        Validate.notNull(def, "The default value must not be null!");

        T value = def;
        value = (T)this.get(name, index, def.getClass());
        return value;
    }

    public <T> T get(String name, Class<T> type)
    {
        return this.get(name, 0, type);
    }

    public <T> T get(String name, T def)
    {
        return this.get(name, 0, def);
    }

    public String getString(String name, int index)
    {
        return this.get(name, index, String.class);
    }

    public String getString(String name, int index, String def)
    {
        String value = this.getString(name, index);
        if (value == null)
        {
            return def;
        }
        return value;
    }

    public String getString(String name)
    {
        return this.getString(name, 0);
    }

    public String getString(String name, String def)
    {
        return this.getString(name, 0, def);
    }
}
