package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.config.annotations.Updater;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This abstract Codec can be implemented to read and write configuratons.
 */
public abstract class ConfigurationCodec
{
    protected String COMMENT_PREFIX;
    protected String SPACES;
    protected String LINEBREAK;
    protected String QUOTE;
    protected Integer revision = null;
    private CodecContainer container = null;
    protected boolean first;

    /**
     * Loads in the given configuration using the inputstream
     *
     * @param config the config to load
     * @param is     the inputstream to load from
     */
    public void load(Configuration config, InputStream is) throws InstantiationException, IllegalAccessException
    {
        container = new CodecContainer();
        container.fillFromInputStream(is);
        Revision a_revision = config.getClass().getAnnotation(Revision.class);
        if (a_revision != null)
        {
            if (revision != null)
            {
                if (config.getClass().isAnnotationPresent(Updater.class))
                {
                    if (a_revision.value() > revision)
                    {
                        Updater updater = config.getClass().getAnnotation(Updater.class);
                        updater.value().newInstance().update(container.values, revision);
                    }
                }
            }
        }
        container.dumpIntoFields(config, container.values, config.parent);
        container = null;
        revision = null;
    }

    /**
     * Returns the current CodecContainer
     *
     * @return the CodecContainer
     */
    public CodecContainer getContainer()
    {
        return this.container;
    }

    /**
     * Returns the offset as String
     *
     * @param offset
     * @return the offset
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
     * Saves the configuration into given file
     *
     * @param config the configuration to save
     * @param file   the file to save into
     */
    public void save(Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            container = new CodecContainer();
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                this.revision = a_revision.value();
                container.values.put("revision", this.revision);
            }
            container.values = container.fillFromFields(null, null, config, "", container.values);
            container.saveIntoFile(file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
        container = null;
    }

    public void saveChildConfig(Configuration parentConfig, Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            container = new CodecContainer();
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                this.revision = a_revision.value();
                container.values.put("revision", this.revision);
            }
            container.values = container.fillFromFields(null, parentConfig, config, "", container.values);
            container.saveIntoFile(file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
        container = null;
    }

    /**
     * Serializes the values in the map
     *
     * @param path   the path of the map
     * @param values the values at given path
     * @param off    the current offset
     * @return the serialized map
     */
    public abstract String convertMap(String path, Map<String, Object> values, int off);

    /**
     * Serializes a single value
     *
     * @param path  the path of the value
     * @param value the value at given path
     * @param off   the current offest
     * @return the serialized value
     */
    public abstract String convertValue(String path, Object value, int off);

    /**
     * Builds a the comment for given path
     *
     * @param path the path
     * @param off  the current offset
     * @return the comment
     */
    public abstract String buildComment(String path, int off);

    /**
     * Returns the FileExtension as String
     *
     * @return the fileExtension
     */
    public abstract String getExtension();

    /**
     * Converts the inputStream into a String->Object map
     *
     * @param is the inputstream
     * @return the loaded values
     */
    public abstract Map<String, Object> loadFromInputStream(InputStream is);

    /**
     * Returns the last subKey of this path
     *
     * @param path
     * @return the last subKey
     */
    public String getSubKey(String path)
    {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    /**
     * Returns the subPath of this path
     *
     * @param path
     * @return the subPath
     */
    public String getSubPath(String path)
    {
        return path.substring(path.indexOf('.') + 1);
    }

    /**
     * Returns the baseParh of this path
     *
     * @param path
     * @return the basePath
     */
    public String getBasePath(String path)
    {
        return path.substring(0, path.indexOf('.'));
    }

    /**
     * This class temporarily holds the values/comments of the configuration to
     * save or load them.
     */
    public class CodecContainer
    {
        protected Map<String, Object> values;
        protected Map<String, String> comments;
        protected Map<String, Object> loadedKeys;
        protected Configuration       config;
        protected String              currentPath;
        protected CodecContainer      parentContainer = null;
        private Configuration         parentConfig;

        public CodecContainer()
        {
            this.comments = new THashMap<String, String>();
            this.loadedKeys = new THashMap<String, Object>();
            this.values = new LinkedHashMap<String, Object>();
        }

        /**
         * Fills the map with values form the inputStream
         *
         * @param is
         */
        public void fillFromInputStream(InputStream is)
        {
            if (is == null)
            {
                this.values = new LinkedHashMap<String, Object>(); // InputStream null -> config was not existent
            }
            else
            {
                this.values = loadFromInputStream(is); // Load from InputStream
            }
            if (this.values == null)
            {
                this.values = new LinkedHashMap<String, Object>(); // loadValues null -> config exists but was empty
            }
            this.loadKeys(this.values);
        }

        /**
         * Converts given object at path to fit into to the field
         *
         * @param object
         * @param field
         * @param path
         * @return the converted object
         * @throws ConversionException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        public Object convertFromObjectToFieldValue(Object object, Field field, String path) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            Type fieldType = field.getGenericType();
            if (fieldType instanceof Class)
            {
                return Convert.fromObject(fieldType, object);
            }
            if (fieldType instanceof ParameterizedType // can be a map?
                && ((ParameterizedType)fieldType).getRawType() instanceof Class // rawIsClass
                && Map.class.isAssignableFrom((Class)((ParameterizedType)fieldType).getRawType()) // isMap
                && ((ParameterizedType)fieldType).getActualTypeArguments()[1] instanceof Class // mapValue is class
                && Configuration.class.isAssignableFrom(((Class)((ParameterizedType)fieldType).getActualTypeArguments()[1]))) // mapValue is Config
            {
                Type valType = ((ParameterizedType)fieldType).getActualTypeArguments()[1];
                if (valType instanceof Class)
                {
                    Map<Object, ? extends Configuration> fieldMap = (Map)field.get(this.config);
                    Map<String, Object> subvalues = this.getOrCreateSubSection(path, values);
                    for (Object key : fieldMap.keySet())
                    {
                        new CodecContainer().dumpIntoFields(fieldMap.get(key), this.getOrCreateSubSection(key.toString(), subvalues),
                            parentConfig == null ? null : ((Map<Object, Configuration>)(field.get(this.parentConfig))).get(key));
                    }
                    return fieldMap;
                }
            }
            return Convert.fromObject(fieldType, object); // this covers the genericConversion
        }

        /**
         * Sets the fields with the loaded values
         *
         * @param config
         * @param section
         * @throws ConversionException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        private void dumpIntoFields(Configuration config, Map<String, Object> section, Configuration parent)
        {
            this.config = config;
            this.parentConfig = parent;
            this.values = section;
            for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
            {
                try
                {
                    if (field.isAnnotationPresent(Option.class))
                    {
                        String path = field.getAnnotation(Option.class).value();
                        if (Configuration.class.isAssignableFrom(field.getType()))
                        {
                            Configuration subConfig = (Configuration)field.get(this.config);
                            CodecContainer subContainer = new CodecContainer();
                            if (parent == null)
                            {
                                subContainer.dumpIntoFields(subConfig, this.getOrCreateSubSection(path, section), null);
                            }
                            else
                            {
                                subContainer.dumpIntoFields(subConfig, this.getOrCreateSubSection(path, section), (Configuration)field.get(parent));
                            }
                            continue;
                        }
                        int mask = field.getModifiers();
                        if (((mask & Modifier.FINAL) == Modifier.FINAL) || (((mask & Modifier.STATIC) == Modifier.STATIC))) // skip static and final fields
                        {
                            continue;
                        }
                        Object object = this.get(field.getAnnotation(Option.class).value().toLowerCase(Locale.ENGLISH), section);//Get savedValue or default
                        if (object != null)
                        {
                            field.set(config, convertFromObjectToFieldValue(object, field, path));//Set loaded Value into Field
                        }
                        else if (this.parentConfig != null)
                        {
                            field.set(config, field.get(this.parentConfig)); // If not found get from parentConfig (if childconfig)
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new InvalidConfigurationException("Error while dumping loaded Config into fields! Field: " + field.getName(), e);
                }
            }
        }

        /**
         * Saves the values into a file
         *
         * @param file
         * @throws IOException
         */
        private void saveIntoFile(File file) throws IOException
        {
            FileWriter writer = new FileWriter(file);
            try
            {
                writer.write(this.dumpIntoString());
            }
            finally
            {
                writer.close();
            }
        }

        /**
         * Converts the values into a String to save
         *
         * @return the converted map
         */
        private String dumpIntoString()
        {
            StringBuilder sb = new StringBuilder();
            if (config.head() != null)
            {
                sb.append("# ").append(StringUtils.implode("\n# ", config.head())).append(LINEBREAK).append(LINEBREAK);
            }
            first = true;
            sb.append(convertMap("", values, 0).trim()).append("\n");
            if (config.tail() != null)
            {
                sb.append("# ").append(StringUtils.implode("\n# ", config.tail()));
            }
            return sb.toString();
        }

        /**
         * Gets the value at given path in the section
         *
         * @param path
         * @param section
         * @return the requested value
         */
        private Object get(String path, Map<String, Object> section)
        {
            if (section == null || section.isEmpty())
            {
                return null;
            }
            if (path.contains("."))
            {
                return this.get(getSubPath(path), (Map<String, Object>)section.get(this.findKey(getBasePath(path))));
            }
            return section.get(this.findKey(path));
        }

        /**
         * Sets the value to given path in the section
         *
         * @param path
         * @param value
         * @param section
         */
        private void set(String path, Object value, Map<String, Object> section)
        {
            if (path.contains("."))
            {
                Map<String, Object> subsection = this.getOrCreateSubSection(getBasePath(path), section);
                this.set(getSubPath(path), value, subsection);
            }
            else
            {
                section.put(path, value);
            }
        }

        /**
         * Returns the subSection in the baseSection for given path
         *
         * @param path
         * @param basesection
         * @return the requested subSection
         */
        private Map<String, Object> getOrCreateSubSection(String path, Map<String, Object> basesection)
        {
            if (path.contains("."))
            {
                Map<String, Object> subsection = this.getOrCreateSubSection(getBasePath(path), basesection);
                basesection.put(getBasePath(path), subsection);
                return this.getOrCreateSubSection(getSubPath(path), subsection);
            }
            else
            {
                Map<String, Object> subsection = (Map<String, Object>)basesection.get(path);
                if (subsection == null)
                {
                    subsection = new LinkedHashMap<String, Object>();
                    basesection.put(path, subsection);
                }
                return subsection;
            }
        }

        /**
         * Returns the loadedKey coressponding to a lowercased key
         *
         * @param key
         * @return the coressponding key
         */
        private Object findKey(String key)
        {
            Object foundKey = this.loadedKeys.get(key);
            if (foundKey == null)
            {
                return key;
            }
            return foundKey;
        }

        /**
         * saves the loaded keys into a map lowerCaseKey->Key
         *
         * @param section
         */
        private void loadKeys(Map<String, Object> section)
        {
            for (Object key : section.keySet()) //need object ere because key could be an integer when in a subMap
            {
                this.loadedKeys.put(key.toString().toLowerCase(Locale.ENGLISH), key);
                if (section.get(key) instanceof Map)
                {
                    this.loadKeys((Map<String, Object>)section.get(key));
                }
            }
        }

        /**
         * adds a comment to later save
         *
         * @param path
         * @param comment
         */
        private void addComment(String path, String comment)
        {
            if (parentContainer == null)
            {
                this.comments.put(path, comment);
            }
            else
            {
                parentContainer.addComment(path, comment);
            }
        }

        /**
         * gets the comment for given path
         *
         * @param path
         * @return
         */
        public String getComment(String path)
        {
            return this.comments.get(path);
        }

        /**
         * Fills the map with values from the Fields to save
         *
         * @param config
         * @param basePath
         * @param section
         * @return the filled map
         * @throws IllegalArgumentException
         * @throws ConversionException
         * @throws IllegalAccessException
         */
        public Map<String, Object> fillFromFields(CodecContainer parentContainer, Configuration parentConfig, Configuration config, String basePath, Map<String, Object> section) throws IllegalArgumentException, ConversionException, IllegalAccessException
        {
            this.parentContainer = parentContainer;
            this.parentConfig = parentConfig;
            this.config = config;
            if (parentConfig != null)
            {
                if (!parentConfig.getClass().equals(config.getClass()))
                {
                    throw new IllegalStateException("parent and child-config have to be the same type of config!");
                }
            }
            Class<? extends Configuration> clazz = config.getClass();
            if (clazz.isAnnotationPresent(MapComments.class))
            {
                MapComment[] mapcomments = clazz.getAnnotation(MapComments.class).value();
                for (MapComment comment : mapcomments)
                {
                    if ("".equals(basePath))
                    {
                        this.addComment(comment.path(), comment.text());
                    }
                    else
                    {
                        this.addComment(basePath + "." + comment.path(), comment.text());
                    }
                }
            }
            boolean advanced = true;
            try
            {
                Field field = clazz.getField("advanced");
                advanced = field.getBoolean(config);
            }
            catch (Exception ignored)
            {}
            for (Field field : clazz.getFields())
            {
                if (field.isAnnotationPresent(Option.class))
                {
                    if (!advanced && field.getAnnotation(Option.class).advanced())
                    {
                        continue;
                    }
                    int mask = field.getModifiers();
                    if (((mask & Modifier.FINAL) == Modifier.FINAL) || (((mask & Modifier.STATIC) == Modifier.STATIC)))
                    {
                        continue;
                    }
                    String path = field.getAnnotation(Option.class).value();
                    if (field.isAnnotationPresent(Comment.class))
                    {
                        Comment comment = field.getAnnotation(Comment.class);
                        if ("".equals(basePath))
                        {
                            this.addComment(path, comment.value());
                        }
                        else
                        {
                            this.addComment(basePath + "." + path, comment.value());
                        }
                    }
                    path = path.toLowerCase(Locale.ENGLISH);
                    Object value = this.convertFromFieldValueToObject(field, field.get(config), basePath, this.getOrCreateSubSection(path, section));
                    if (parentConfig != null)
                    {
                        Object parentValue = this.convertFromFieldValueToObject(field, field.get(parentConfig), basePath, this.getOrCreateSubSection(path, section));
                        if (parentValue.equals(value))
                        {
                            this.remove(path, section);
                            continue;
                        }
                    }
                    this.set(path, value, section);
                }
            }
            return section;
        }

        private void remove(String path, Map<String, Object> section)
        {
            if (path.contains("."))
            {
                Map<String, Object> subsection = this.getOrCreateSubSection(getBasePath(path), section);
                this.remove(getSubPath(path), subsection);
                if (subsection.isEmpty())
                {
                    section.remove(getBasePath(path));
                }
            }
            else
            {
                section.remove(path);
            }
        }

        /**
         * Converts a field value into a serializable Object
         *
         * @param field
         * @param fieldValue
         * @param basepath
         * @param section
         * @return the converted fieldvalue
         * @throws ConversionException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        public Object convertFromFieldValueToObject(Field field, Object fieldValue, String basepath, Map<String, Object> section) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            if (fieldValue == null)
            {
                return null;
            }
            Class fieldClass = fieldValue.getClass();
            if (fieldValue instanceof Configuration)
            {
                String newPath = field.getAnnotation(Option.class).value();
                if (!basepath.isEmpty())
                {
                    newPath = basepath + "." + newPath;
                }
                if (this.parentConfig == null)
                {
                    return new CodecContainer().fillFromFields(this, null, (Configuration)fieldValue, newPath, this.getOrCreateSubSection(basepath, section));
                }
                return new CodecContainer().fillFromFields(this, (Configuration)field.get(this.parentConfig), (Configuration)fieldValue, newPath, this.getOrCreateSubSection(basepath, section));
            }
            Converter converter = Convert.matchConverter(fieldClass);
            if (converter == null)
            {
                if (Map.class.isAssignableFrom(fieldClass))
                {
                    if (!((Map)fieldValue).isEmpty() && ((Map)fieldValue).values().iterator().next() instanceof Configuration)
                    {
                        Map<Object, ? extends Configuration> fieldMap = (Map)fieldValue;
                        Map<String, Object> map = new LinkedHashMap<String, Object>();
                        Converter keyConverter = Convert.matchConverter(fieldMap.keySet().iterator().next().getClass());
                        for (Object key : fieldMap.keySet())
                        {
                            String newPath = field.getAnnotation(Option.class).value() + "." + this.findKey(key.toString());
                            if (!basepath.isEmpty())
                            {
                                newPath = basepath + "." + newPath;
                            }
                            map.put(keyConverter.toObject(key).toString(), //the key should be a string or else it will fail horribly
                            new CodecContainer().fillFromFields(this, this.parentConfig, fieldMap.get(key),
                                newPath, this.getOrCreateSubSection(key.toString(), map)));
                        }
                        return map;
                    }
                }
                return Convert.toObject(fieldValue); // array / collection / map-converter
            }
            else
            {
                return converter.toObject(fieldValue);
            }
        }
    }
}
