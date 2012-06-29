package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.SectionComment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.*;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.yaml.YamlConfiguration;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
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
    protected AbstractConfiguration config;
    protected File file;
    private static final HashMap<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();
    private CubeLogger logger = CubeEngine.getLogger();

    static
    {
        Converter converter = new ShortConverter();
        registerConverter(Short.class, converter);
        registerConverter(short.class, converter);

        converter = new ByteConverter();
        registerConverter(Byte.class, converter);
        registerConverter(byte.class, converter);

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
            converter = this.matchConverter(fieldClass);
            if (converter == null)
            {
                if (Collection.class.isAssignableFrom(fieldClass))
                {
                    Collection<?> list = (Collection<?>)object;
                    if (list.isEmpty())
                    {
                        return object;
                    }
                    Class<?> genType = field.getAnnotation(Option.class).genericType();
                    converter = this.matchConverter(genType);
                    if (converter != null)
                    {
                        Collection<Object> result;
                        try
                        {
                            result = (Collection)field.get(this);
                            result.clear();
                        }
                        catch (Exception ex)
                        {
                            this.logger.severe("Error while converting to " + genType.toString());
                            return null;
                        }
                        for (Object o : list)
                        {
                            result.add(converter.to(o));
                        }
                        return result;
                    }
                }
            }
        }
        if (converter == null)
        {
            return object;
        }
        return converter.to(object);
    }

    public Converter matchConverter(Class<?> objectClass)
    {
        Converter converter;
        for (Class<?> clazz : converters.keySet())
        {
            if (clazz.isAssignableFrom(objectClass))
            {
                converter = converters.get(clazz);
                registerConverter(objectClass, converter);
                return converter;
            }
        }
        return null;
    }

    public Object convertFrom(Field field, Object object)
    {
        Class<?> objectClass = object.getClass();
        Converter converter = converters.get(objectClass);
        if (converter == null)
        {
            converter = this.matchConverter(objectClass);
            if (converter == null)
            {
                if (Collection.class.isAssignableFrom(objectClass))
                {
                    Collection<?> collection = (Collection<?>)object;
                    Class<?> genType = field.getAnnotation(Option.class).genericType();
                    converter = this.matchConverter(genType);
                    if (converter != null)
                    {
                        Collection<Object> result = new ArrayList<Object>();
                        for (Object o : collection)
                        {
                            result.add(converter.from(o));
                        }
                        return result;
                    }
                }
            }
        }
        if (converter == null)
        {
            return object;
        }
        return converter.from(object);
    }

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
            CubeEngine.getLogger().severe("Error while loading a Configuration!");
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
            this.logger.severe("Error while loading a Configuration-File!");
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
            this.logger.severe("Error while saving a Configuration-File!");
        }
        this.config.finish(); //clears saved comments/values
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
        this.config.finish();
        this.saveConfiguration();
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
                        this.config.set(path, this.convertFrom(field, field.get(this)));
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
            this.logger.severe("Error while loading a Configuration-Element!");
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
            Map<String, Object> section = (Map<String, Object>)field.get(this);
            //get saved Values from ConfigFile
            Map<String, Object> loadedSection = (Map<String, Object>)config.get(path);
            if (loadedSection == null || loadedSection.keySet().isEmpty())
            {
                this.config.set(path, section);//Set default values
                return; //Nothing loaded! Default value is already set!
            }
            field.set(this, loadedSection);//Set Field with loaded Values
        }
        catch (IllegalAccessException ex)
        {
            this.logger.severe("Error while loading a Configuration-Section!");
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
                String path = field.getAnnotation(Option.class).value();
                if (field.isAnnotationPresent(Comment.class))
                {
                    this.config.addComment(path, field.getAnnotation(Comment.class).value());
                }
                if (field.isAnnotationPresent(SectionComment.class))
                {
                    SectionComment scomment = field.getAnnotation(SectionComment.class);
                    this.config.addComment(scomment.path(), scomment.text());
                }
                this.config.set(path, this.convertFrom(field, field.get(this)));
            }
        }
        catch (IllegalAccessException ex)
        {
            this.logger.severe("Error while saving a Configuration-Element!");
        }
    }
}
