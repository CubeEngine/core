package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Type;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.*;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.json.JsonConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.yaml.YamlConfiguration;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 */
public abstract class Configuration
{
    protected ConfigurationRepresenter config;
    protected File file;
    private static final HashMap<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();
    private CubeLogger logger = CubeEngine.getLogger();
    private static final HashMap<String, ConfigurationRepresenter> configtypes = new HashMap<String, ConfigurationRepresenter>();
    protected boolean readonly = false;

    static
    {
        Converter converter = new ShortConverter();
        registerConverter(Short.class, converter);
        registerConverter(short.class, converter);

        converter = new ByteConverter();
        registerConverter(Byte.class, converter);
        registerConverter(byte.class, converter);

        converter = new IntegerConverter();
        registerConverter(Integer.class, converter);
        registerConverter(int.class, converter);

        converter = new DoubleConverter();
        registerConverter(Double.class, converter);
        registerConverter(double.class, converter);

        registerConverter(OfflinePlayer.class, new PlayerConverter());
        registerConverter(Location.class, new LocationConverter());

        registerConfigType("yml", new YamlConfiguration());
        registerConfigType("json", new JsonConfiguration());
    }

    /**
     * Registers given Converter for clazz
     *
     * @param clazz the class
     * @param converter the converter
     */
    public static void registerConverter(Class<?> clazz, Converter converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        converters.put(clazz, converter);
    }

    /**
     * Registers a Configuration for given extension
     *
     * @param extension the extension
     * @param config the config
     */
    public static void registerConfigType(String extension, ConfigurationRepresenter config)
    {
        configtypes.put(extension, config);
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
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

    /**
     * Converts the object to fit into the field
     *
     * @param field the field
     * @param object the object to deserialize
     * @return the deserialized object
     */
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
                            this.logger.log(Level.SEVERE, "Error while converting to {0}", genType.toString());
                            return null;
                        }
                        for (Object o : list)
                        {
                            result.add(converter.to(o));
                        }
                        return result;
                    }
                }
                if (Map.class.isAssignableFrom(fieldClass))
                {
                    Map<String, ?> map = (Map<String, ?>)object;
                    if (map.isEmpty())
                    {
                        return object;
                    }
                    Class<?> genType = field.getAnnotation(Option.class).genericType();
                    converter = this.matchConverter(genType);
                    if (converter != null)
                    {
                        Map<String, Object> result;
                        try
                        {
                            result = (Map<String, Object>)field.get(this);
                            result.clear();
                        }
                        catch (Exception ex)
                        {
                            this.logger.log(Level.SEVERE, "Error while converting to {0}", genType.toString());
                            return null;
                        }
                        for (String key : map.keySet())
                        {
                            result.put(key, converter.to(map.get(key)));
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

    /**
     * Converts the field to fit into the object
     *
     * @param field the field to serialize
     * @param object the object
     * @return the serialized fieldvalue
     */
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
     * Loads the Configuration | if needed set default Values and save
     */
    public void loadConfiguration()
    {
        for (Field field : this.getClass().getFields())
        {
            this.loadElement(field);
        }
        this.config.clear(); //Clear loaded Maps
        if (!readonly)
        {
            this.saveConfiguration();
        }
    }

    /**
     * Loads in the config from File
     */
    public void loadFromFile()
    {
        try
        {
            this.config.load(file);
        }
        catch (Throwable t)
        {
            this.logger.log(Level.SEVERE, "Error while loading a Configuration-File!", t);
        }
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

                if (field.isAnnotationPresent(Comment.class))
                {
                    this.config.addComment(path, field.getAnnotation(Comment.class).value());
                }
                if (field.isAnnotationPresent(MapComment.class))
                {
                    MapComment comment = field.getAnnotation(MapComment.class);
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
     * Saves the Configuration
     */
    public void saveConfiguration()
    {
        if (readonly)
        {
            throw new IllegalStateException("Tried to save Readonly-Configuration");
        }
        for (Field field : this.getClass().getFields())
        {
            this.saveElement(field);
        }
        this.saveToFile();
    }

    /**
     * Saves the Configuration to the File
     */
    public void saveToFile()
    {
        if (readonly)
        {
            throw new IllegalStateException("Tried to save Readonly-Configuration");
        }
        try
        {
            this.config.save(this.file);
        }
        catch (IOException ex)
        {
            this.logger.severe("Error while saving a Configuration-File!");
        }
        this.config.clear(); //clears saved comments/values
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
                if (field.isAnnotationPresent(MapComment.class))
                {
                    MapComment mcomment = field.getAnnotation(MapComment.class);
                    this.config.addComment(mcomment.path(), mcomment.text());
                }
                this.config.set(path, this.convertFrom(field, field.get(this)));
            }
        }
        catch (IllegalAccessException ex)
        {
            this.logger.severe("Error while saving a Configuration-Element!");
        }
    }

    private static ConfigurationRepresenter getConfigurationType(String fileExtension)
    {
        ConfigurationRepresenter configuration = configtypes.get(fileExtension);
        if (configuration == null)
        {
            throw new IllegalStateException("FileExtension ." + fileExtension + "+ cannot be used for Configurations!");
        }
        return configuration;
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
            Type type = clazz.getAnnotation(Type.class);
            if (type == null)
            {
                throw new IllegalStateException("Configuration Type undefined!");
            }
            config.file = file;
            config.config = getConfigurationType(type.value());
            config.loadFromFile();
            config.loadConfiguration(); //Load in config and/or set default values
            return config;
        }
        catch (Throwable t)
        {
            CubeEngine.getLogger().log(Level.SEVERE, "Error while loading a Configuration!", t);
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
        Type type = clazz.getAnnotation(Type.class);
        if (type == null)
        {
            throw new IllegalStateException("Configuration Type undefined!");
        }
        return load(new File(module.getCore().getFileManager().getConfigDir(), module.getModuleName() + "." + type.value()), clazz);
    }

    /**
     * Returns the loaded Configuration
     * 
     * 
     * @param is the Inputstream to load the config from
     * @param clazz the Configuration to use
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(InputStream is, Class<T> clazz)
    {
        try
        {
            Type type = clazz.getAnnotation(Type.class);
            if (type == null)
            {
                throw new IllegalStateException("Configuration Type undefined!");
            }
            T config = clazz.newInstance();
            config.config = getConfigurationType(type.value());
            config.config.load(is);
            config.readonly = true;
            config.loadConfiguration(); //set values loaded from inputStream
            config.readonly = false;
            return config;
        }
        catch (Throwable t)
        {
            CubeEngine.getLogger().log(Level.SEVERE, "Error while loading a Configuration!", t);
            return null;
        }
    }

    public void setConfigurationType(String fileExtension)
    {
        ConfigurationRepresenter newconfig = configtypes.get(fileExtension);
        if (newconfig == null)
        {
            CubeEngine.getLogger().log(Level.SEVERE, "FileExtension .{0} cannot be used for Configurations!", fileExtension);
            return;
        }
        this.config = newconfig;
    }

    /**
     * Sets the file to load from
     *
     * @param file
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Sets the Configuration to readonly!
     */
    public void setReadOnly()
    {
        this.readonly = true;
    }

    public void onLoaded()
    {
    }
}
