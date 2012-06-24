package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 */
public abstract class Configuration
{
    protected YamlConfiguration config;
    protected File file;
    private static final HashMap<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();

    public Configuration()
    {
        registerConverter(short.class, new ShortConverter());
        registerConverter(Short.class, new ShortConverter());
        registerConverter(byte.class, new ByteConverter());
        registerConverter(Byte.class, new ByteConverter());
        registerConverter(OfflinePlayer.class, new PlayerConverter());
        registerConverter(Location.class, new LocationConverter());
    }

    public static void registerConverter(Class<?> clazz, Converter converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        converters.put(clazz, converter);
    }

    public Object convertTo(Field field, Object object)
    {
        Class<?> fieldClass = field.getType();
        Converter converter = converters.get(fieldClass);
        if (converter == null)
        {
            for (Class<?> clazz : converters.keySet())
            {
                if (clazz.isAssignableFrom(fieldClass))
                {
                    converter = converters.get(fieldClass);
                    registerConverter(fieldClass, converter);
                    break;
                }
            }
        }
        if (converter == null)
        {
            return object;
        }
        return converter.to(object);
    }

    public Object convertFrom(Object object)
    {
        Class<?> objectClass = object.getClass();
        Converter converter = converters.get(objectClass);
        if (converter == null)
        {
            for (Class<?> clazz : converters.keySet())
            {
                if (clazz.isAssignableFrom(objectClass))
                {
                    converter = converters.get(clazz);
                    registerConverter(objectClass, converter);
                    break;
                }
            }
        }
        if (converter == null)
        {
            return object;
        }
        return converter.from(object);
    }

    /*
     *
     * if (clazz.equals(Short.class) || clazz.equals(short.class)) { converter =
     * new ShortConverter(); } else if (clazz.equals(Byte.class) ||
     * clazz.equals(byte.class)) { converter = new ByteConverter(); } else if
     * (clazz.equals(OfflinePlayer.class)) { converter = new PlayerConverter();
     * } else if (clazz.equals(Location.class)) { converter = new
     * LocationConverter(); }
     *
     * if (converter == null) { return configElem; } return
     * converter.to(configElem); }
     *
     * public static Object convertFrom(Object object) { Converter converter =
     * null; if (object instanceof Short) { converter = new ShortConverter(); }
     * else if (object instanceof Byte) { converter = new ByteConverter(); }
     * else if (object instanceof OfflinePlayer) { converter = new
     * PlayerConverter(); } else if (object instanceof Location) { converter =
     * new LocationConverter(); }
     *
     */
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
            t.printStackTrace();
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
            t.printStackTrace();
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
            ex.printStackTrace();
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
                        this.config.set(path, this.convertFrom(field.get(this)));
                    }
                    else
                    {
                        //Set new Field Value
                        field.set(this, this.convertTo(field, configElem));
                    }
                }
                if (field.isAnnotationPresent(Comment.class))
                {
                    this.config.addComment(path, field.getAnnotation(Comment.class).value());
                }
                if (field.isAnnotationPresent(SectionComment.class))
                {
                    SectionComment comment = field.getAnnotation(SectionComment.class);
                    this.config.addComment(comment.path(), comment.text());
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            System.out.println("Error while loading a Configuration-Element!");
            ex.printStackTrace();
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
            Map<String, Object> loadedSection = configSection.getValues();
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
            ex.printStackTrace();
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
        for (String key : configSection.getKeys())
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }
}
