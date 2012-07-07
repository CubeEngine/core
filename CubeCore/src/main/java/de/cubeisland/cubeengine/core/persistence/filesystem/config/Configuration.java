package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Type;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.codec.JsonCodec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.codec.YamlCodec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.*;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
    private static final Map<String, ConfigurationCodec> codecs = new HashMap<String, ConfigurationCodec>();
    protected static final Logger logger = CubeEngine.getLogger();
    protected ConfigurationCodec codec = null;
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

        registerConverter(Set.class, new SetConverter());

        registerCodec("yml", new YamlCodec());
        registerCodec("json", new JsonCodec());
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
     * @param codec the codec
     */
    public static void registerCodec(String extension, ConfigurationCodec codec)
    {
        codecs.put(extension, codec);
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
    private static Converter matchConverter(Class<?> objectClass)
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
    public static Object convertTo(Configuration config, Field field, Object object)
    {
        Class<?> fieldClass = field.getType();
        Converter converter = converters.get(fieldClass);
        if (converter == null)
        {
            converter = matchConverter(fieldClass);
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
                        converter = matchConverter(genType);
                        if (converter != null)
                        {
                            Collection<Object> result;
                            try
                            {
                                result = (Collection)field.get(config);
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
                        converter = matchConverter(genType);
                        if (converter != null)
                        {
                            Map<String, Object> result;
                            try
                            {
                                result = (Map<String, Object>)field.get(config);
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
                        converter = matchConverter(genType);
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
    public static Object convertFrom(Field field, Object object)
    {
        Class<?> objectClass = object.getClass();
        Converter converter = converters.get(objectClass);
        if (converter == null)
        {
            converter = matchConverter(objectClass);
            if (converter == null)
            {
                if (Collection.class.isAssignableFrom(objectClass))
                {
                    if (object instanceof Collection)
                    {
                        Collection<?> collection = (Collection<?>)object;
                        Class<?> genType = field.getAnnotation(Option.class).genericType();
                        converter = matchConverter(genType);
                        if (converter != null)
                        {
                            Collection<Object> result = new LinkedList<Object>();
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
                    converter = matchConverter(genType);
                    if (converter != null)
                    {
                        Collection<Object> result = new LinkedList<Object>();
                        for (Object o : array)
                        {
                            result.add(converter.from(o));
                        }
                        return result;
                    }
                    else
                    {
                        Collection<Object> result = new LinkedList<Object>();
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
     * Saves the Configuration
     */
    public void saveConfiguration()
    {
        this.codec.save(this, this.file);
    }

    public static ConfigurationCodec resolveCodec(String fileExtension)
    {
        ConfigurationCodec codec = codecs.get(fileExtension);
        if (codec == null)
        {
            throw new IllegalStateException("FileExtension ." + fileExtension + " cannot be used for Configurations!");
        }
        return codec;
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
     * @param is the Inputstream to load the codec from
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
            config.setCodec(type.value());
            if (is != null)
            {
                config.codec.load(config, is); //load config in maps -> updates -> sets fields
            }
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

    public ConfigurationCodec getCodec()
    {
        return this.codec;
    }

    public void setCodec(String fileExtension)
    {
        this.codec = resolveCodec(fileExtension);
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
