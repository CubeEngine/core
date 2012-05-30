package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.module.Module;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

/**
 *
 * @author Faithcaio
 */
public abstract class ModuleConfiguration
{
    protected final CubeConfiguration config;

    public ModuleConfiguration(Module module)
    {
        config = module.getCore().getFileManager().getModuleConfig(module);
    }

    /**
     * Returns the CubeConfiguration usually you do not need this!
     *
     * @return the CubeConfiguration
     */
    @Deprecated
    public CubeConfiguration getCubeConfiguration()
    {
        return this.config;
    }

    /**
     * Loads the Configuration | if needed set default Values and save
     */
    public void loadConfiguration()
    {
        try
        {
            //Loading config from file
            config.load();
        }
        catch (FileNotFoundException ex)
        {
            //TODO
        }
        catch (IOException ex)
        {
            //TODO
        }
        catch (InvalidConfigurationException ex)
        {
            //TODO
        }

        for (Field field : this.getClass().getFields())
        {
            //set all declared Fields & if needed set default values
            this.loadElement(field);
        }
        config.safeSave();
    }

    /**
     * Loads in a ConfigurationElement from the ConfigurationFile into an Object
     * (or a Map(String->Object))
     *
     * @param field the field to load
     */
    private void loadElement(Field field)
    {
        try
        {
            if (field.isAnnotationPresent(Option.class))
            {
                String path = field.getAnnotation(Option.class).value();
                if (Map.class.isAssignableFrom(field.getType()))
                {
                    //Field is a Map
                    this.loadSection(field);
                }
                else
                {
                    //Get savedValue or default
                    Object configElem = config.get(path);
                    if (configElem == null)
                    {
                        //Set defaultValue if no value saved
                        config.addDefault(path, field.get(this));
                        return; //Field Value is already set to default
                    }
                    //Set new Field Value
                    field.set(this, configElem);
                }
            }
        }
        catch (IllegalAccessException ex)
        {
        }
    }

    /**
     * Loads in a ConfigurationSection from the ConfigurationFile into a
     * Map(String->Object)
     *
     * @param field the field to load
     */
    private void loadSection(Field field)
    {
        try
        {
            String path = field.getAnnotation(Option.class).value();
            //get Default Keys
            Map<String, Object> section = (Map<String, Object>) field.get(this);
            //get saved Values from ConfigFile
            ConfigurationSection configSection = config.getConfigurationSection(path);
            if (configSection == null)
            {
                //if section is not yet created: Create it
                configSection = config.createSection(path);
            }
            Map<String, Object> loadedSection = configSection.getValues(true);
            for (String key : section.keySet())
            {
                //Check if all Keys were loaded | If not: set to Default Key
                if (!loadedSection.containsKey(key))
                {
                    configSection.addDefault(key, section.get(key));
                    loadedSection.put(key, section.get(key));
                }
            }
            //Set Field with loaded Values
            field.set(this, loadedSection);
        }
        catch (IllegalAccessException ex)
        {
        }
    }

    /**
     * Saves the Configuration
     */
    public void saveConfiguration()
    {
        for (Field field : this.getClass().getFields())
        {
            this.saveElement(field);
        }
        config.safeSave();
    }

    /**
     * Saves a field(Object) into the config
     *
     * @param field the field to save
     */
    private void saveElement(Field field)
    {
        try
        {
            if (field.isAnnotationPresent(Option.class))
            {
                if (Map.class.isAssignableFrom(field.getType()))
                {
                    this.saveSection(field);
                }
                String path = field.getAnnotation(Option.class).value();
                config.set(path, field.get(this));
            }
        }
        catch (IllegalAccessException ex)
        {
        }
    }

    /**
     * Saves a field(Map) into the config
     *
     * @param field the field to save
     */
    private void saveSection(Field field)
    {
        try
        {
            String path = field.getAnnotation(Option.class).value();
            Map<String, Object> section = (Map<String, Object>) field.get(this);
            ConfigurationSection configSection = config.getConfigurationSection(path);
            for (String key : section.keySet())
            {
                configSection.set(key, section.get(key));
            }
        }
        catch (IllegalAccessException ex)
        {
        }
    }
}
