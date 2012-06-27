package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public abstract class AbstractConfiguration extends ConfigurationSection
{
    public static String COMMENT_PREFIX;
    public static String SPACES;
    public static String LINEBREAK;
    public static String QUOTE;

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

    public void convertMapsToSections(Map<?, ?> input, ConfigurationSection section)
    {
        for (Map.Entry<?, ?> entry : input.entrySet())
        {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map)
            {
                convertMapsToSections((Map<?, ?>)value, section.createSection(key));
                return;
            }
            section.set(key, value);
        }
    }

    /**
     * Converts the Configuration into a String
     *
     * @return the config as String
     */
    public String convertConfig()
    {
        return this.convertSection(this, 0, true);
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

    public abstract void loadFromString(String contents);

    /**
     * Converts a Section into a String
     *
     * @param section the section to convert
     * @param offset the offset
     * @param first whether this is the first section
     * @return
     */
    public abstract String convertSection(ConfigurationSection section, int offset, boolean first);

    /**
     * Builds a Comment
     *
     * @param section
     * @param path
     * @param offset
     * @param first
     * @return
     */
    public abstract String buildComment(ConfigurationSection section, String path, int offset, boolean first);
}
