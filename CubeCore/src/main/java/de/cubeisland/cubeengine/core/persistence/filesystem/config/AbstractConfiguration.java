package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.apache.commons.lang.Validate;

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

    public void load(File file) throws FileNotFoundException, IOException
    {
        Validate.notNull(file, "File cannot be null");
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

    public void load(InputStream stream) throws IOException
    {
        Validate.notNull(stream, "Stream cannot be null");

        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);
        try
        {
            String line;

            while ((line = input.readLine()) != null)
            {
                builder.append(line);
                builder.append('\n');
            }
        }
        finally
        {
            input.close();
        }

        loadFromString(builder.toString());
    }

    public void save(File file) throws IOException
    {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
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

    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section)
    {
        for (Map.Entry<?, ?> entry : input.entrySet())
        {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map)
            {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            }
            else
            {
                section.set(key, value);
            }
        }
    }

    public String convertConfig()
    {
        return this.convertSection(this, 0, true);
    }

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

    public abstract String convertSection(ConfigurationSection section, int offset, boolean first);

    public abstract String buildComment(ConfigurationSection section, String path, int offset, boolean first);
}
