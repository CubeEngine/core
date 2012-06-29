package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public abstract class AbstractConfiguration
{
    public static String COMMENT_PREFIX;
    public static String SPACES;
    public static String LINEBREAK;
    public static String QUOTE;
    protected LinkedHashMap<String, Object> values;
    protected HashMap<String, String> comments;
    protected boolean first;

    public AbstractConfiguration()
    {
        this.comments = new HashMap<String, String>();
        this.values = new LinkedHashMap<String, Object>();
    }

    /**
     * Loads the Configuration from a File
     *
     * @param file the file to load
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void load(File file) throws FileNotFoundException, IOException
    {
        if (file == null)
        {
            return;
        }
        try
        {
            load(new FileInputStream(file));
        }
        catch (FileNotFoundException ex)
        {
            System.out.println(file.getName() + " not found! Creating new config...");
            //TODO msg no config found create new from default
        }
    }

    /**
     * Loads the Configuration from a InputStream
     *
     * @param stream the InputStream
     * @throws IOException
     */
    public void load(InputStream stream) throws IOException
    {
        if (stream == null)
        {
            return;
        }
        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);
        try
        {
            String line;

            while ((line = input.readLine()) != null)
            {
                builder.append(line);
                builder.append(LINEBREAK);
            }
        }
        finally
        {
            input.close();
        }
        loadFromString(builder.toString());
    }

    /**
     * Saves the configuration to a File
     *
     * @param file the File
     * @throws IOException
     */
    public void save(File file) throws IOException
    {
        if (file == null)
        {
            return;
        }
        String data = this.convertConfig();
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write(data);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Converts the Configuration into a String
     *
     * @return the config as String
     */
    public String convertConfig()
    {
        StringBuilder sb = new StringBuilder();
        first = true;
        for (String key : this.values.keySet())
        {
            sb.append(this.convertValue(key, this.values.get(key), 0));
        }
        return sb.toString();
    }

    /**
     * Gets the offset as String
     *
     * @param offset the offset
     * @return the offset as String
     */
    public String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(SPACES);
        }
        return off.toString();
    }

    /**
     * Converts a whole Section/Map into String
     *
     * @param path the current path
     * @param values the values saved in the Section/Map
     * @param off the offset
     * @return the Section/Map as String
     */
    public abstract String convertSection(String path, Map<String, Object> values, int off);

    /**
     *
     * @param path the current path
     * @param value the value saved at given path
     * @param off the offset
     * @return the Value as String
     */
    public abstract String convertValue(String path, Object value, int off);

    /**
     * Converts the inputString into ConfigurationMaps
     *
     * @param contents
     */
    public abstract void loadFromString(String contents);

    /**
     * Builds a Comment for the given path
     *
     * @param path the path
     * @return the comment for path
     */
    public abstract String buildComment(String path, int off);

    /**
     * Gets the value saved under the key
     *
     * @param path the key
     * @return the value or null if no value saved
     */
    public Object get(String path)
    {
        if (path.contains("."))
        {
            return this.get(this.getSubKey(path), (Map<String, Object>)this.values.get(this.getBaseKey(path)));
        }
        else
        {
            return this.values.get(path);
        }
    }

    /**
     * Gets the value saved under this key in given section
     *
     * @param key the key
     * @param section the section
     * @return the value
     */
    public Object get(String key, Map<String, Object> section)
    {
        if (section == null)
        {
            return null;
        }
        if (key.contains("."))
        {
            return this.get(this.getSubKey(key), (Map<String, Object>)section.get(this.getBaseKey(key)));
        }
        else
        {
            return section.get(key);
        }
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
            Map<String, Object> subsection = this.createSection(this.values, this.getBaseKey(key));
            this.set(subsection, this.getSubKey(key), value);
        }
        else
        {
            values.put(key, value);
        }
    }

    /**
     * Sets the value for the key in the specified section
     *
     * @param section the section
     * @param key the key
     * @param value the value to set
     */
    public void set(Map<String, Object> section, String key, Object value)
    {
        if (key.contains("."))
        {
            Map<String, Object> subsection = this.createSection(section, this.getBaseKey(key));
            this.set(subsection, this.getSubKey(key), value);
        }
        else
        {
            section.put(key, value);
        }
    }

    /**
     * Gets or create the section with the key in the basesection
     *
     * @param basesection the basesection
     * @param key the key of the section
     * @return the section
     */
    public Map<String, Object> createSection(Map<String, Object> basesection, String key)
    {
        Map<String, Object> subsection = (Map<String, Object>)basesection.get(key);
        if (subsection == null)
        {
            subsection = new LinkedHashMap<String, Object>();
            basesection.put(key, subsection);
        }
        return subsection;

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
     * Splits up the Key and returns the last SubKey
     *
     * @param key the key
     * @return the last SubKey
     */
    public String getLastSubKey(String key)
    {
        return key.substring(key.lastIndexOf(".") + 1);
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
     * Adds a Comment to the specified path
     *
     * @param path the path
     * @param comment the comment
     */
    public void addComment(String path, String comment)
    {
        this.comments.put(path, comment);
    }

    /**
     * Resets the Maps
     */
    public void finish()
    {
        this.comments.clear();
        this.values.clear();
    }
}
