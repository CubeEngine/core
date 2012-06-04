package de.cubeisland.cubeengine.core.persistence.filesystem;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Faithcaio
 */
public abstract class Configuration
{
    protected YamlConfiguration config;
    protected File file;

    /**
     * Returns the loaded Configuration
     *
     * @param file the configurationfile
     * @param clazz the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(File file, Class<T> clazz)
    {
        try
        {
            T config = clazz.newInstance();
            config.file = file;
            config.config = new YamlConfiguration();
            config.reload();
            config.loadConfiguration(); //Load in config and/or set default values
            return config;
        }
        catch (Throwable t)
        {
            System.out.println("Error while loading a Configuration!");
            if (CubeCore.debugMode)
            {
                t.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Returns the loaded Configuration
     *
     * @param module the module to load the configuration from
     * @param clazz the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Module module, Class<T> clazz)
    {
        return load(new File(module.getCore().getFileManager().getConfigDir(), module.getModuleName() + ".yml"), clazz);
    }

    /**
     * Loads in the config from File
     */
    public void reload()
    {
        try
        {
            this.config.load(file);
        }
        catch (Throwable t)
        {
            System.out.println("Error while loading a Configuration-File!");
            if (CubeCore.debugMode)
            {
                t.printStackTrace();
            }
        }
    }

    /**
     * Saves the Configuration to the File
     */
    public void save()
    {
        try
        {
            this.config.save(this.file);
        }
        catch (IOException ex)
        {
            System.out.println("Error while saving a Configuration-File!");
            if (CubeCore.debugMode)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Loads the Configuration | if needed set default Values and save
     */
    public void loadConfiguration()
    {
        for (Field field : this.getClass().getFields())
        {
            //set all declared Fields & if needed set default values
            this.loadElement(field);
        }
        this.save();
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
                        this.config.set(path, field.get(this));
                        return; //Field Value is already set to default
                    }
                    //Set new Field Value
                    if ((Short.class == field.getType())
                            || short.class == field.getType())
                    {
                        field.set(this, ((Integer) configElem).shortValue());
                    }
                    else if ((Byte.class == field.getType())
                            || byte.class == field.getType())
                    {
                        field.set(this, ((Integer) configElem).byteValue());
                    }
                    else
                    {
                        field.set(this, configElem);
                    }
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            System.out.println("Error while loading a Configuration-Element!");
            if (CubeCore.debugMode)
            {
                ex.printStackTrace();
            }
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
                configSection = this.config.createSection(path);
            }
            Map<String, Object> loadedSection = configSection.getValues(false);
            for (String s : loadedSection.keySet())
            {
                if (loadedSection.get(s) instanceof ConfigurationSection)
                {
                    ConfigurationSection subsection = (ConfigurationSection) loadedSection.get(s);
                    loadedSection.put(s, this.getSection(subsection));
                }
            }
            for (String key : section.keySet())
            {
                //Check if all Keys were loaded | If not: set to Default Key
                if (!loadedSection.containsKey(key))
                {
                    configSection.set(key, section.get(key));
                    loadedSection.put(key, section.get(key));
                }
            }
            //Set Field with loaded Values
            field.set(this, loadedSection);
        }
        catch (IllegalAccessException ex)
        {
            System.out.println("Error while loading a Configuration-Section!");
            if (CubeCore.debugMode)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Loads in a Section (and its SubSections)
     *
     * @param configSection the Section to load
     * @return the loaded Section
     */
    private Map<String, Object> getSection(ConfigurationSection configSection)
    {
        Map<String, Object> section = new HashMap<String, Object>();
        for (String key : configSection.getKeys(false))
        {
            Object value = configSection.get(key);
            if (value instanceof ConfigurationSection)
            {
                value = this.getSection((ConfigurationSection) value);
            }
            section.put(key, value);
        }
        return section;
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
        this.save();
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
            System.out.println("Error while saving a Configuration-Element!");
            if (CubeCore.debugMode)
            {
                ex.printStackTrace();
            }
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
            System.out.println("Error while saving a Configuration-Section!");
            if (CubeCore.debugMode)
            {
                ex.printStackTrace();
            }
        }
    }
}
