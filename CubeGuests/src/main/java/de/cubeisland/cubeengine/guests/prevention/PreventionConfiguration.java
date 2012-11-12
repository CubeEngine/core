package de.cubeisland.cubeengine.guests.prevention;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PreventionConfiguration extends YamlConfiguration
{
    private final File file;
    private static final String FILE_EXTENTION = ".yml";

    private PreventionConfiguration(File file)
    {
        this.file = file;
    }

    public static PreventionConfiguration get(File dir, Prevention prevention)
    {
        return get(dir, prevention, true);
    }

    public static PreventionConfiguration get(File dir, Prevention prevention, boolean load)
    {
        if (dir == null || prevention == null)
        {
            throw new IllegalArgumentException("dir and prevention both must not be null!");
        }
        dir.mkdirs();
        if (!dir.isDirectory())
        {
            throw new IllegalArgumentException("dir must represent a directory!");
        }

        final PreventionConfiguration config = new PreventionConfiguration(new File(dir, prevention.getName() + FILE_EXTENTION));
        config.options().header(prevention.getConfigHeader());
        if (load)
        {
            try
            {
                config.load();
            }
            catch (FileNotFoundException e)
            {}
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            catch (InvalidConfigurationException e)
            {
                e.printStackTrace(System.err);
            }
        }
        config.options().copyDefaults(true);
        config.setDefaults(prevention.getDefaultConfig());
        return config;
    }

    public void load() throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        this.load(this.file);
    }


    public void save() throws IOException
    {
        this.save(this.file);
    }

    public boolean safeSave()
    {
        try
        {
            this.save();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }
}
