package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Revision;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Type;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.*;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.representer.*;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 * @author Phillip Schichtel
 */
public abstract class Configuration
{
    private static final Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();
    private static final Map<String, ConfigurationRepresenter> representers = new HashMap<String, ConfigurationRepresenter>();
    private static final Logger logger = CubeEngine.getLogger();
    protected ConfigurationRepresenter representer = null;
    protected File file;

    static
    {
        Converter converter;

        converter = new ByteConverter();
        registerConverter(Byte.class, converter);
        registerConverter(byte.class, converter);

        converter = new ShortConverter();
        registerConverter(Short.class, converter);
        registerConverter(short.class, converter);

        converter = new IntegerConverter();
        registerConverter(Integer.class, converter);
        registerConverter(int.class, converter);

        converter = new DoubleConverter();
        registerConverter(Double.class, converter);
        registerConverter(double.class, converter);

        registerConverter(OfflinePlayer.class, new PlayerConverter());
        registerConverter(Location.class, new LocationConverter());

        registerType("yml", new YamlRepresenter());
        registerType("json", new JsonRepresenter());
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
     * @param representer the representer
     */
    public static void registerType(String extension, ConfigurationRepresenter config)
    {
        representers.put(extension, config);
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
    private Converter matchConverter(Class<?> objectClass)
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
    private Object convertTo(Field field, Object object)
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
                    if (object instanceof Collection)
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
                                logger.log(Level.SEVERE, "Error while converting to {0}", genType.toString());
                                return null;
                            }
                            for (Object o : list)
                            {
                                result.add(converter.to(o));
                            }
                            return result;
                        }
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Could not apply Collection for {0}", field.getName());
                    }
                }
                if (Map.class.isAssignableFrom(fieldClass))
                {
                    if (object instanceof Map)
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
                                logger.log(Level.SEVERE, "Error while converting to {0}", genType.toString());
                                return null;
                            }
                            for (String key : map.keySet())
                            {
                                result.put(key, converter.to(map.get(key)));
                            }
                            return result;
                        }
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Could not apply Map for {0}", field.getName());
                    }
                }
                if (fieldClass.isArray())
                {
                    if (object instanceof Collection)
                    {
                        Collection<Object> coll = (Collection)object;
                        Object tmparray = coll.toArray();
                        Class<?> genType = field.getAnnotation(Option.class).genericType();
                        converter = this.matchConverter(genType);
                        if (converter != null)
                        {
                            Object o = Array.newInstance(genType, coll.size());
                            for (int i = 0; i < coll.size(); ++i)
                            {
                                Array.set(o, i, converter.to(Array.get(tmparray, i)));
                            }
                            return fieldClass.cast(o);
                        }
                        else
                        {
                            Object o = Array.newInstance(genType, coll.size());
                            for (int i = 0; i < coll.size(); ++i)
                            {
                                Array.set(o, i, Array.get(tmparray, i));
                            }
                            return fieldClass.cast(o);
                        }
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Could not apply Array for {0}", field.getName());
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
    private Object convertFrom(Field field, Object object)
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
                    if (object instanceof Collection)
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
                    else
                    {
                        logger.log(Level.WARNING, "Could not apply Collection to {0}", field.getName());
                    }
                }
                if (objectClass.isArray())
                {
                    Object[] array = (Object[])object;
                    Class<?> genType = field.getAnnotation(Option.class).genericType();
                    converter = this.matchConverter(genType);
                    if (converter != null)
                    {
                        Collection<Object> result = new ArrayList<Object>();
                        for (Object o : array)
                        {
                            result.add(converter.from(o));
                        }
                        return result;
                    }
                    else
                    {
                        Collection<Object> result = new ArrayList<Object>();
                        result.addAll(Arrays.asList(array));
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
        this.representer.clear(); //Clear loaded Maps
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
                Object configElem = representer.get(path);
                if (configElem == null)
                {
                    //Set defaultValue if no value saved
                    this.representer.set(path, this.convertFrom(field, field.get(this)));
                }
                else
                {
                    //Set new Field Value
                    field.set(this, this.convertTo(field, configElem));
                }

                if (field.isAnnotationPresent(Comment.class))
                {
                    this.representer.addComment(path, field.getAnnotation(Comment.class).value());
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            logger.severe("Error while loading a Configuration-Element!");
        }
    }

    /**
     * Saves the Configuration
     */
    public void saveConfiguration()
    {
        if (this.file == null)
        {
            logger.warning("Tried to save config without File. No Saving...");
            return;//No File -> No Saving
        }
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(MapComments.class))
        {
            MapComment[] comments = clazz.getAnnotation(MapComments.class).value();
            for (MapComment comment : comments)
            {
                this.representer.addComment(comment.path(), comment.text());
            }
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
        try
        {
            this.representer.save(this.file);
        }
        catch (IOException e)
        {
            logger.severe("Error while saving a Configuration-File!");
        }
        this.representer.clear(); //clears saved comments/values
    }

    /**
     * Saves a field(Object) into the representer
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
                    this.representer.addComment(path, field.getAnnotation(Comment.class).value());
                }
                this.representer.set(path, this.convertFrom(field, field.get(this)));
            }
        }
        catch (IllegalAccessException ex)
        {
            logger.severe("Error while saving a Configuration-Element!");
        }
    }

    public static ConfigurationRepresenter resolveRepresenter(String fileExtension)
    {
        ConfigurationRepresenter representer = representers.get(fileExtension);
        if (representer == null)
        {
            throw new IllegalStateException("FileExtension ." + fileExtension + " cannot be used for Configurations!");
        }
        return representer;
    }

    /**
     * Loads and returns the loaded Configuration
     *
     * @param file the configurationfile
     * @param clazz the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(File file, Class<T> clazz)
    {
        Validate.notNull(file, "The file must not be null!");
        try
        {
            if (file == null)
            {
                return null;
            }
            InputStream is = null;
            try
            {
                is = new FileInputStream(file);
            }
            catch (FileNotFoundException ex)
            {
                logger.log(Level.INFO, "{0} not found! Creating new config...", file.getName());
            }
            T config = load(is, clazz); //loading config from InputSream or Default
            config.file = file;
            config.saveConfiguration();
            return config;
        }
        catch (Throwable t)
        {
            logger.log(Level.SEVERE, "Error while loading a Configuration!", t);
            return null;
        }
    }

    /**
     * Loads and returns the loaded Configuration
     *
     * @param is the Inputstream to load the representer from
     * @param clazz the Configuration to use
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(InputStream is, Class<T> clazz)
    {
        try
        {
            Revision revis = clazz.getAnnotation(Revision.class);
            if (revis == null)
            {
                //No Revision do nothing...
            }
            else
            {
                //Check Revision of InputStream
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader input = new BufferedReader(reader);
                String firstline = input.readLine();
                //TODO interpretiere 1.Zeile ... oder letzte ka wie ich das mache bei verschiedenen configtypen ohne Fehler beim einles später
            }
            Type type = clazz.getAnnotation(Type.class);
            if (type == null)
            {
                throw new IllegalStateException("Configuration Type undefined!");
            }
            T config = clazz.newInstance();
            config.setRepresenter(type.value());
            if (is != null)
            {
                config.representer.load(is);
            }
            config.loadConfiguration(); //set values loaded from inputStream
            config.onLoaded();
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
            //ConfigType undefined
            return null;
        }
        return load(new File(module.getCore().getFileManager().getConfigDir(), module.getName() + "." + type.value()), clazz);
    }

    public ConfigurationRepresenter getRepresenter()
    {
        return this.representer;
    }

    public void setRepresenter(String fileExtension)
    {
        this.representer = resolveRepresenter(fileExtension);
    }

    /**
     * Sets the file to load from
     *
     * @param file
     */
    public void setFile(File file)
    {
        Validate.notNull(file, "The file must not be null!");
        this.file = file;
    }

    public File getFile()
    {
        return this.file;
    }

    public void onLoaded()
    {
    }
}
