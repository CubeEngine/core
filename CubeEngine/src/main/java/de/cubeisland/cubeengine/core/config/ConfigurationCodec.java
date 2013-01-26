package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.config.annotations.Updater;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.converter.generic.CollectionConverter;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This abstract Codec can be implemented to read and write configurations.
 */
public abstract class ConfigurationCodec
{
    protected String COMMENT_PREFIX;
    protected String SPACES;
    protected String LINE_BREAK;
    protected String QUOTE;
    protected final String PATH_SEPARATOR = ":";
    protected Integer revision = null;
    protected boolean first;

    /**
     * Loads in the given configuration using the InputStream
     *
     * @param config the config to load
     * @param is the InputStream to load from
     */
    public void load(Configuration config, InputStream is) throws InstantiationException, IllegalAccessException
    {
        CodecContainer container = new CodecContainer();
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
     * Returns the offset as String TODO naming...
     *
     * @param offset the offset
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
     * @param file the file to save into
     */
    public void save(Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            CodecContainer container = new CodecContainer();
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
    }

    public void saveChildConfig(Configuration parentConfig, Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            CodecContainer container = new CodecContainer();
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
    }

    /**
     * Serializes the values in the map
     *
     * @param path the path of the map
     * @param values the values at given path
     * @param off the current offset
     * @return the serialized map
     */
    public abstract String convertMap(String path, Map<String, Object> values, int off, boolean inCollection);

    /**
     * Serializes a single value
     *
     * @param path the path of the value
     * @param value the value at given path
     * @param off the current offset
     * @return the serialized value
     */
    public abstract String convertValue(String path, Object value, int off, boolean inCollection);

    /**
     * Builds a the comment for given path
     *
     * @param path the path
     * @param off the current offset
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
     * Converts the inputStream into a readable Object
     *
     * @param is the InputStream
     * @return the loaded values
     */
    public abstract Node loadFromInputStream(InputStream is);

    /**
     * Returns the last subKey of this path
     *
     * @param path the path
     * @return the last subKey
     */
    public String getSubKey(String path)
    {
        return path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
    }

    /**
     * Returns the subPath of this path
     *
     * <p>Example: first.second.third -> second.third
     *
     * @param path the path
     * @return the subPath
     */
    public String getSubPath(String path)
    {
        return path.substring(path.indexOf(PATH_SEPARATOR) + 1);
    }

    /**
     * Returns the base path of this path
     *
     * <p>Example: main.sub -> main
     *
     * @param path the path
     * @return the basePath
     */
    public String getBasePath(String path)
    {
        return path.substring(0, path.indexOf(PATH_SEPARATOR));
    }

    /**
     * This class temporarily holds the values/comments of the configuration to
     * save or load them.
     */
    public class CodecContainer
    {
        protected Node values;
        protected Map<String, String> comments;
        protected Map<String, Object> loadedKeys;
        protected Configuration config;
        protected String currentPath;
        protected CodecContainer parentContainer = null;
        private Configuration parentConfig;

        public CodecContainer()
        {
            this.comments = new THashMap<String, String>();
            this.loadedKeys = new THashMap<String, Object>();
        }

        /**
         * Fills the map with values form the inputStream
         *
         * @param is an InputStream
         */
        public void fillFromInputStream(InputStream is)
        {
            if (is == null)
            {
                this.values = MapNode.emptyMap(); // InputStream null -> config was not existent
            }
            else
            {
                this.values = loadFromInputStream(is); // Load from InputStream
            }
            if (this.values == null)
            {
                this.values = MapNode.emptyMap();; // loadValues null -> config exists but was empty
            }
            this.loadKeys(this.values);
        }

        /**
         * Converts given object at path to fit into to the field
         *
         * @param object the object containing the field
         * @param field the field
         * @param path the path
         * @return the converted object
         * @throws ConversionException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        @SuppressWarnings("unchecked")
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
                    Map<Object, Configuration> fieldMap = (Map<Object, Configuration>)field.get(this.config);
                    Map<String, Object> subValues = this.getOrCreateObjectAt(path, values);
                    for (Object key : fieldMap.keySet())
                    {
                        new CodecContainer().dumpIntoFields(fieldMap.get(key), this.getOrCreateObjectAt(key.toString(), subValues),
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
         * @param config the config
         * @param currentSection the section to load from
         * @throws IllegalArgumentException
         */
        private void dumpIntoFields(Configuration config, Object currentSection, Configuration parent)
        {
            this.config = config;
            this.parentConfig = parent;
            for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
            {
                try
                {
                    if (field.isAnnotationPresent(Option.class))
                    {
                        String path = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                        if (Configuration.class.isAssignableFrom(field.getType()))
                        {
                            Configuration subConfig = (Configuration)field.get(this.config).getClass().newInstance();
                            CodecContainer subContainer = new CodecContainer();
                            if (parent == null)
                            {
                                subContainer.dumpIntoFields(subConfig, this.getOrCreateObjectAt(path, currentSection), null);
                            }
                            else
                            {
                                subContainer.dumpIntoFields(subConfig, this.getOrCreateSectionAt(path, currentSection), (Configuration)field.get(parent));
                            }
                            continue;
                        }
                        if (field.getGenericType() instanceof ParameterizedType)
                        {
                            ParameterizedType ptype = (ParameterizedType)field.getGenericType();

                            if (Collection.class.isAssignableFrom((Class)ptype.getRawType()))
                            {
                                Type subType1 = ptype.getActualTypeArguments()[0];
                                if (Configuration.class.isAssignableFrom((Class)subType1))
                                {
                                    Collection loadedValues = this.getOrCreateObjectAt(path, section).values();
                                    if (parent == null)
                                    {//No parent given: iterate throuh given configs & load them

                                        for (Configuration subConfig : (Collection<Configuration>) field.get(config))
                                        {
                                            new CodecContainer().dumpIntoFields(subConfig, this.getOrCreateObjectAt(path, section), null);
                                        }
                                    }
                                    else
                                    {
                                        Collection collection = CollectionConverter.getCollectionFor(ptype); // Get correct CollectionType
                                        field.set(config, collection);
                                    }

                                }
                            }
                            else if (Map.class.isAssignableFrom((Class)ptype.getRawType()))
                            {
                                Type subType2 = ptype.getActualTypeArguments()[1];
                                if (Configuration.class.isAssignableFrom((Class)subType2))
                                {
                                    //TODO subconfigs in map
                                }
                            }

                        }
                        //TODO handle subconfigs in maps/collections
                        int mask = field.getModifiers();
                        if ((((mask & Modifier.STATIC) == Modifier.STATIC))) // skip static fields
                        {
                            continue;
                        }
                        Object object = this.get(field.getAnnotation(Option.class).value().toLowerCase(Locale.ENGLISH).replace(".", PATH_SEPARATOR), section);//Get savedValue or default
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
                    throw new InvalidConfigurationException(
                            "Error while dumping loaded config into fields!" +
                            "\ncurrent config: "+config.getClass().toString()+
                            "\ncurent field:" +field.getName(),e);
                }
            }
        }

        /**
         * Saves the values into a file
         *
         * @param file the file to save to
         * @throws IOException
         */
        private void saveIntoFile(File file) throws IOException
        {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.append(this.dumpIntoString());
            writer.flush();
            writer.close();
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
                sb.append("# ").append(StringUtils.implode("\n# ", config.head())).append(LINE_BREAK).append(LINE_BREAK);
            }
            first = true;
            sb.append(convertMap("", values, 0, false).trim()).append("\n");
            if (config.tail() != null)
            {
                sb.append("# ").append(StringUtils.implode("\n# ", config.tail()));
            }
            return sb.toString();
        }

        /**
         * Gets the value at given path in the section
         *
         * @param path the path
         * @param section the section
         * @return the requested value
         */
        @SuppressWarnings("unchecked")
        private Object get(String path, Map<String, Object> section)
        {
            if (section == null || section.isEmpty())
            {
                return null;
            }
            if (path.contains(PATH_SEPARATOR))
            {
                return this.get(getSubPath(path), (Map<String, Object>)section.get(this.findKey(getBasePath(path))));
            }
            return section.get(this.findKey(path));
        }

        /**
         * Sets the value to given path in the section
         *
         * @param path the path
         * @param value the value
         * @param section the section
         */
        private void set(String path, Object value, Map<String, Object> section)
        {
            if (path.contains(PATH_SEPARATOR))
            {
                Map<String, Object> subsection = this.getOrCreateObjectAt(getBasePath(path), section);
                this.set(getSubPath(path), value, subsection);
            }
            else
            {
                section.put(path, value);
            }
        }

        /**
         * Returns the section located in the currentSection for given path
         *
         * @param path the path
         * @param currentSection the section to search in
         * @return the requested object
         */
        private Object getOrCreateSectionAt(String path, Object currentSection)
        {
            return this.getOrCreateObjectAt(path,currentSection,true);
        }

        /**
         * Returns the Object located in the currentSection for given path
         *
         * @param path the path
         * @param currentSection the section to search in
         * @param createMap whether to create a map or a collection if not found
         * @return the requested object
         */
        private Object getOrCreateObjectAt(String path, Object currentSection, boolean createMap)
        {
            if (path.contains(PATH_SEPARATOR))
            {
                return this.getOrCreateObjectAt(getSubPath(path),
                        this.getOrCreateObjectAt(getBasePath(path), currentSection, createMap),
                        createMap);
            }
            else
            {
                if (currentSection instanceof Map)
                {
                    Object subsection = ((Map)currentSection).get(path);
                    if (subsection == null)
                    {
                        if (createMap)
                        {
                            subsection = new LinkedHashMap<String, Object>();
                        }
                        else
                        {
                            subsection = new ArrayList<Object>();
                        }
                        ((Map)currentSection).put(path, subsection);
                    }
                    return subsection;
                }
                else
                {
                    throw new IllegalArgumentException("Could not resolve path ("+path+") for "+currentSection);
                }
            }
        }

        /**
         * Returns the loadedKey corresponding to a lower cased key
         *
         * @param key the key
         * @return the corresponding key
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
         * @param section the section
         */
        @SuppressWarnings("unchecked")
        private void loadKeys(Object input)
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
         * @param path the path
         * @param comment the comment
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
         * @param path the path of the comment
         * @return the comment
         */
        public String getComment(String path)
        {
            return this.comments.get(path);
        }

        /**
         * Fills the map with values from the Fields to save
         *
         * @param parentContainer the parent container
         * @param parentConfig the parent config
         * @param config the config
         * @param basePath the base path
         * @param section the section
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
                MapComment[] mapComments = clazz.getAnnotation(MapComments.class).value();
                for (MapComment comment : mapComments)
                {
                    if ("".equals(basePath))
                    {
                        this.addComment(comment.path(), comment.text());
                    }
                    else
                    {
                        this.addComment(basePath + PATH_SEPARATOR + comment.path(), comment.text());
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
                    String path = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                    if (field.isAnnotationPresent(Comment.class))
                    {
                        Comment comment = field.getAnnotation(Comment.class);
                        if ("".equals(basePath))
                        {
                            this.addComment(path, comment.value());
                        }
                        else
                        {
                            this.addComment(basePath + PATH_SEPARATOR + path, comment.value());
                        }
                    }
                    path = path.toLowerCase(Locale.ENGLISH);
                    Object value = this.convertFromFieldValueToObject(field, field.get(config), basePath, this.getOrCreateObjectAt(path, section));
                    if (parentConfig != null)
                    {
                        Object parentValue = this.convertFromFieldValueToObject(field, field.get(parentConfig), basePath, this.getOrCreateObjectAt(path, section));
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
            if (path.contains(PATH_SEPARATOR))
            {
                Map<String, Object> subsection = this.getOrCreateObjectAt(getBasePath(path), section);
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
         * @param field the field
         * @param fieldValue the value
         * @param basePath the base path
         * @param section the section
         * @return the converted field value
         * @throws ConversionException
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         */
        @SuppressWarnings("unchecked")
        public Object convertFromFieldValueToObject(Field field, Object fieldValue, String basePath, Map<String, Object> section) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            if (fieldValue == null)
            {
                return null;
            }
            Class fieldClass = fieldValue.getClass();
            if (fieldValue instanceof Configuration)
            {
                String newPath = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                if (!basePath.isEmpty())
                {
                    newPath = basePath + PATH_SEPARATOR + newPath;
                }
                if (this.parentConfig == null)
                {
                    return new CodecContainer().fillFromFields(this, null, (Configuration)fieldValue, newPath, this.getOrCreateObjectAt(basePath, section));
                }
                return new CodecContainer().fillFromFields(this, (Configuration)field.get(this.parentConfig), (Configuration)fieldValue, newPath, this.getOrCreateObjectAt(basePath, section));
            }
            Converter converter = Convert.matchConverter(fieldClass);
            if (converter == null)
            {
                if (Map.class.isAssignableFrom(fieldClass))
                {
                    if (!((Map)fieldValue).isEmpty() && ((Map)fieldValue).values().iterator().next() instanceof Configuration)
                    {
                        Map<Object, Configuration> fieldMap = (Map<Object, Configuration>)fieldValue;
                        Map<String, Object> map = new LinkedHashMap<String, Object>();
                        Converter keyConverter = Convert.matchConverter(fieldMap.keySet().iterator().next().getClass());
                        Map<Object, Configuration> parentConfigs = null;
                        if (this.parentConfig != null)
                        {
                            parentConfigs = ((Map<Object, Configuration>)field.get(this.parentConfig));
                        }
                        for (Object key : fieldMap.keySet())
                        {
                            String newPath = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR) + PATH_SEPARATOR + this.findKey(key.toString());
                            if (!basePath.isEmpty())
                            {
                                newPath = basePath + PATH_SEPARATOR + newPath;
                            }
                            Configuration parentSubConfig = parentConfig == null ? null : parentConfigs.get(key);
                            map.put(keyConverter.toObject(key).toString(), //the key should be a string or else it will fail horribly
                            new CodecContainer().fillFromFields(this, parentSubConfig,
                                        fieldMap.get(key), newPath, this.getOrCreateObjectAt(key.toString(), map)));
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
