package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Revision;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Updater;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.ByteConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.Converter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.DoubleConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.IntegerConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.LocationConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.PlayerConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.SetConverter;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.converter.ShortConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 */
public abstract class ConfigurationCodec
{
    private static final Map<Class<?>, Converter> converters = new HashMap<Class<?>, Converter>();
    public String COMMENT_PREFIX;
    public String SPACES;
    public String LINEBREAK;
    public String QUOTE;
    protected LinkedHashMap<String, Object> values;
    protected HashMap<String, String> comments;
    protected boolean first;
    protected Integer revision = null;
    private static final Logger logger;

    static
    {
        logger = CubeEngine.getLogger();
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
    }

    public ConfigurationCodec()
    {
        this.comments = new HashMap<String, String>();
        this.values = new LinkedHashMap<String, Object>();
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
     * Loads the Configuration from a InputStream
     *
     * @param is the InputStream
     * @throws IOException
     */
    public void load(Configuration config, InputStream is) throws IOException
    {
        this.loadIntoCodec(is);//load config into Codec
        this.updateConfig(config.getClass());//update loaded config in Codec if needed
        this.loadIntoFields(config);//update Fields with loaded values
        this.clear(); //clear loaded values
    }

    /**
     * Reads the InputStream to load the config into this Codec
     *
     * @param is the InputStream
     * @throws IOException
     */
    private void loadIntoCodec(InputStream is) throws IOException
    {
        if (is == null)
        {
            return;
        }
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);
        try
        {
            String line;
            line = input.readLine();
            if (line != null)
            {
                if (!line.startsWith("#Revision:"))//Detect Revision
                {
                    builder.append(line).append(LINEBREAK);
                }
                else
                {
                    try
                    {
                        int rev = Integer.parseInt(line.substring(line.indexOf(" ")));
                        this.revision = rev;
                    }
                    catch (NumberFormatException ex)
                    {
                    }
                }
                while ((line = input.readLine()) != null)
                {
                    builder.append(line);
                    builder.append(LINEBREAK);
                }
            }
        }
        finally
        {
            input.close();
        }
        loadFromString(builder.toString());
    }

    /**
     * Converts the inputString into ConfigurationMaps
     *
     * @param contents
     */
    public abstract void loadFromString(String contents);

    private void updateConfig(Class<? extends Configuration> clazz)
    {
        Revision revis = clazz.getAnnotation(Revision.class);
        if (revis != null && this.revision != null)
        {
            if (revis.value() > this.revision)
            {
                logger.log(Level.INFO, "Updating Configuration from Revision {0}", this.revision);
                Updater annotation = clazz.getClass().getAnnotation(Updater.class);
                if (annotation != null)
                {
                    Class<? extends ConfigurationUpdater> updaterClass = annotation.value();
                    ConfigurationUpdater updater;
                    try
                    {
                        updater = updaterClass.newInstance();
                        this.values = updater.update(this.values, this.revision);
                    }
                    catch (Exception ex)
                    {
                    }
                }
            }
        }
    }

    /**
     * Writes loaded values from the Codec into the fields of the Configuration
     *
     * @param config the Configuration
     */
    private void loadIntoFields(Configuration config)
    {
        for (Field field : config.getClass().getFields())
        {
            try
            {
                if (field.isAnnotationPresent(Option.class))
                {
                    String path = field.getAnnotation(Option.class).value();
                    //Get savedValue or default
                    Object configElem = this.get(path);
                    if (configElem == null)
                    {
                        //Set defaultValue if no value saved
                        this.set(path, convertFrom(field, field.get(config)));
                    }
                    else
                    {
                        //Set new Field Value
                        field.set(config, convertTo(config, field, configElem));
                    }

                    if (field.isAnnotationPresent(Comment.class))
                    {
                        this.addComment(path, field.getAnnotation(Comment.class).value());
                    }
                }
            }
            catch (IllegalAccessException ex)
            {
                logger.severe("Error while loading a Configuration-Element!");
            }
        }
    }

    /**
     * Converts the Configutaion with this Codec and saves into given File
     *
     * @param config the Configutaion
     * @param file the File
     */
    public void save(Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            this.saveIntoCodec(config);//Get Map & Comments
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                this.revision = a_revision.value();
            }
            this.save(file);
            this.clear();
        }
        catch (IOException ex)
        {
            logger.severe("Error while saving a Configuration-File!");
        }
        catch (IllegalAccessException ex)
        {
            logger.severe("Error while saving a Configuration-Element!");
        }
    }

    /**
     * Converts the Configuration into Map String->Object
     *
     * @param config the Configuration
     * @throws IllegalAccessException
     */
    private void saveIntoCodec(Configuration config) throws IllegalAccessException
    {
        Class<? extends Configuration> clazz = config.getClass();
        if (clazz.isAnnotationPresent(MapComments.class))
        {
            MapComment[] mapcomments = clazz.getAnnotation(MapComments.class).value();
            for (MapComment comment : mapcomments)
            {
                this.addComment(comment.path(), comment.text());
            }
        }
        for (Field field : clazz.getFields())
        {
            if (field.isAnnotationPresent(Option.class))
            {
                String path = field.getAnnotation(Option.class).value();
                if (field.isAnnotationPresent(Comment.class))
                {
                    this.addComment(path, field.getAnnotation(Comment.class).value());
                }
                this.set(path, convertFrom(field, field.get(config)));
            }
        }
    }

    /**
     * Saves the configuration to a File
     *
     * @param file the File
     * @throws IOException
     */
    public void save(File file) throws IOException
    {
        if (file == null)
        {
            return;
        }
        String data = this.convertConfigToString();
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

    /**
     * Converts the Configuration into a String for saving
     *
     * @return the config as String
     */
    public String convertConfigToString()
    {
        StringBuilder sb = new StringBuilder();
        first = true;
        sb.append(this.revision());
        sb.append(this.head());
        sb.append(this.convertMap("", this.values, 0));
        sb.append(this.tail());
        return sb.toString();
    }

    /**
     * Converts a whole Section/Map into String for saving
     *
     * @param path the current path
     * @param values the values saved in the Section/Map
     * @param off the offset
     * @return the Section/Map as String
     */
    public abstract String convertMap(String path, Map<String, Object> values, int off);

    /**
     * Converts a Value into String for saving
     *
     * @param path the current path
     * @param value the value saved at given path
     * @param off the offset
     * @return the Value as String
     */
    public abstract String convertValue(String path, Object value, int off);

    /**
     * Builds a Comment for the given path
     *
     * @param path the path
     * @return the comment for path
     */
    public abstract String buildComment(String path, int off);

    /**
     * Gets the offset as String
     *
     * @param offset the offset
     * @return the offset as String
     */
    protected String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(SPACES);
        }
        return off.toString();
    }

    /**
     * Gets the value saved under the path
     *
     * @param path the path
     * @return the value or null if no value saved
     */
    public Object get(String path)
    {
        if (path.contains("."))
        {
            return this.get(this.getSubPath(path), (Map<String, Object>)this.values.get(this.getBasePath(path)));
        }
        else
        {
            return this.values.get(path);
        }
    }

    /**
     * Gets the value saved under this path in given section
     *
     * @param path the path
     * @param section the section
     * @return the value saved under path in section
     */
    private Object get(String path, Map<String, Object> section)
    {
        if (section == null)
        {
            return null;
        }
        if (path.contains("."))
        {
            return this.get(this.getSubPath(path), (Map<String, Object>)section.get(this.getBasePath(path)));
        }
        else
        {
            return section.get(path);
        }
    }

    /**
     * Sets a value at a specified path
     *
     * @param path the path
     * @param value the value to set
     */
    public void set(String path, Object value)
    {
        if (path.contains("."))
        {
            Map<String, Object> subsection = this.createSection(this.values, this.getBasePath(path));
            this.set(subsection, this.getSubPath(path), value);
        }
        else
        {
            values.put(path, value);
        }
    }

    /**
     * Sets the value at the path in the specified section
     *
     * @param section the section
     * @param path the path
     * @param value the value to set
     */
    private void set(Map<String, Object> section, String path, Object value)
    {
        if (path.contains("."))
        {
            Map<String, Object> subsection = this.createSection(section, this.getBasePath(path));
            this.set(subsection, this.getSubPath(path), value);
        }
        else
        {
            section.put(path, value);
        }
    }

    /**
     * Gets or create the section with the path in the basesection
     *
     * @param basesection the basesection
     * @param path the path of the section
     * @return the section
     */
    private Map<String, Object> createSection(Map<String, Object> basesection, String path)
    {
        Map<String, Object> subsection = (Map<String, Object>)basesection.get(path);
        if (subsection == null)
        {
            subsection = new LinkedHashMap<String, Object>();
            basesection.put(path, subsection);
        }
        return subsection;
    }

    /**
     * Splits up the path and returns the basepath
     *
     * @param path the path
     * @return the basepath
     */
    public String getBasePath(String path)
    {
        return path.substring(0, path.indexOf("."));
    }

    /**
     * Splits up the path and returns the subpath
     *
     * @param path the path
     * @return the subpath
     */
    public String getSubPath(String path)
    {
        return path.substring(path.indexOf(".") + 1);
    }

    /**
     * Splits up the path and returns the key
     *
     * @param path the path
     * @return the key
     */
    public String getSubKey(String path)
    {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    /**
     * Adds a Comment to the specified path
     *
     * @param path the path
     * @param comment the comment
     */
    public void addComment(String path, String comment)
    {
        this.comments.put(path, comment);
    }

    /**
     * Resets the value & comments Map
     */
    public void clear()
    {
        this.comments.clear();
        this.values.clear();
    }

    /**
     * Gets the revision as String to put infront of the File
     *
     * @return
     */
    public String revision()
    {
        if (revision != null)
        {
            return new StringBuilder("#Revision: ").append(this.revision).append(LINEBREAK).toString();
        }
        return "";
    }

    /**
     * This is inserted in front of the String to safe
     *
     * @return the head
     */
    public String head()
    {
        return "";
    }

    /**
     * This is appended to the String to safe
     *
     * @return the tail
     */
    public String tail()
    {
        return "";
    }
}
