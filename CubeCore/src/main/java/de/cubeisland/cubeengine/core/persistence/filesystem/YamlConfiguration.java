package de.cubeisland.cubeengine.core.persistence.filesystem;

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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A YamlConfiguration without bukkit
 *
 * @author Faithcaio
 */
public class YamlConfiguration extends ConfigurationSection
{
    protected static final String BLANK_CONFIG = "{}\n";
    private final Yaml yaml;
    private final DumperOptions yamlOptions;
    private final Representer yamlRepresenter;

    public YamlConfiguration()
    {
        super();
        this.yamlOptions = new DumperOptions();
        this.yamlRepresenter = new YamlRepresenter();
        this.yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    }

    public void save(File file) throws IOException
    {
        Validate.notNull(file, "File cannot be null");
        Files.createParentDirs(file);
        String data = this.toString();
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

    public void loadFromString(String contents)
    {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        input = (Map<?, ?>) yaml.load(contents);
        if (input != null)
        {
            convertMapsToSections(input, this);
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
}
