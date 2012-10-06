package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.core.util.converter.Converter;
import de.cubeisland.cubeengine.core.util.converter.GenericConverter;
import de.cubeisland.cubeengine.core.util.converter.MapConverter;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public abstract class ConfigurationCodec
{
    protected Integer revision = null;
    public String COMMENT_PREFIX;
    public String SPACES;
    public String LINEBREAK;
    public String QUOTE;
    private CodecContainer container = null;
    protected boolean first;

    public void load(Configuration config, InputStream is)
    {
        container = new CodecContainer();
        container.fillFromInputStream(is);
        //TODO update config here
        try
        {
            container.dumpIntoFields(config, container.values);
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Error while dumping loaded Config into fields!", e);
        }
        container = null;
    }

    public CodecContainer getContainer()
    {
        return this.container;
    }

    protected String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(SPACES);
        }
        return off.toString();
    }

    public void save(Configuration config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            container = new CodecContainer();
            container.values = container.fillFromFields(config, "", container.values);

            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                this.revision = a_revision.value();
            }
            container.saveIntoFile(file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
        container = null;
    }

    public abstract String convertMap(String path, Map<String, Object> values, int off);

    public abstract String convertValue(String path, Object value, int off);

    public abstract String buildComment(String path, int off);

    public abstract String getExtension();

    public String revision()
    {
        if (revision != null)
        {
            return new StringBuilder("#Revision: ").append(this.revision).append(LINEBREAK).toString();
        }
        return "";
    }

    public abstract Map<String, Object> loadFromInputStream(InputStream is);

    public String getSubKey(String path)
    {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    public class CodecContainer
    {
        protected Map<String, Object> values;
        protected Map<String, String> comments;
        protected Map<String, String> loadedKeys;
        protected Configuration config;
        protected String currentPath;

        public CodecContainer()
        {
            this.comments = new THashMap<String, String>();
            this.loadedKeys = new THashMap<String, String>();
            this.values = new LinkedHashMap<String, Object>();
        }

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

        public Object convertFromObjectToFieldValue(Object object, Field field, String path) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            Class fieldClass = field.getType();
            if (!String.class.isAssignableFrom(fieldClass)) //TODO other types without conversion?
            {
                Converter converter = Convert.matchConverter(fieldClass);
                if (converter == null)
                {
                    Class genericType = field.getAnnotation(Option.class).genericType();
                    GenericConverter gConverter = Convert.matchGenericConverter(fieldClass);
                    if (Configuration.class.isAssignableFrom(genericType))
                    {
                        if (gConverter instanceof MapConverter) // config in maps IMPORTANT: key.toString() is the key in the config
                        {
                            Map<Object, ? extends Configuration> fieldMap = (Map)field.get(this.config);
                            Map<String, Object> subvalues = this.getOrCreateSubSection(path, values);
                            for (Object key : fieldMap.keySet())
                            {
                                new CodecContainer().dumpIntoFields(fieldMap.get(key),
                                    this.getOrCreateSubSection(key.toString(), subvalues));
                            }
                            return fieldMap;
                        }
                        else
                        {
                            throw new InvalidConfigurationException("Configurations can not load inside an array or collection!");
                        }
                    }
                    if (gConverter != null)
                    {
                        return gConverter.fromObject(object, field.get(this.config), genericType);
                    }
                }
                else
                {
                    return converter.fromObject(object);
                }
            }
            return object;

        }

        private void dumpIntoFields(Configuration config, Map<String, Object> section) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            this.config = config;
            for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
            {
                if (field.isAnnotationPresent(Option.class))
                {
                    String path = field.getAnnotation(Option.class).value();
                    if (Configuration.class.isAssignableFrom(field.getType()))
                    {
                        Configuration subConfig = (Configuration)field.get(this.config);
                        CodecContainer subContainer = new CodecContainer();
                        subContainer.dumpIntoFields(subConfig, this.getOrCreateSubSection(path, section));
                        continue;
                    }
                    int mask = field.getModifiers();
                    if ((mask & Modifier.STATIC) == Modifier.STATIC) //skip static fields //TODO final fields too
                    {
                        continue;
                    }
                    Object object = this.get(field.getAnnotation(Option.class).value().toLowerCase(Locale.ENGLISH), section);//Get savedValue or default
                    if (object != null)
                    {
                        field.set(config, convertFromObjectToFieldValue(object, field, path));//Set loaded Value into Field
                    }
                }
            }
        }

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

        private String dumpIntoString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(this.revision());
            if (config.head() != null)
            {
                sb.append("# ").append(StringUtils.implode("\n# ", config.head())).append(LINEBREAK).append(LINEBREAK);
            }
            first = true;
            sb.append(convertMap("", values, 0));
            if (config.tail() != null)
            {
                sb.append("# ").append(StringUtils.implode("\n# ", config.tail()));
            }
            return sb.toString();
        }

        public String revision()
        {
            if (revision != null)
            {
                return new StringBuilder("#Revision: ").append(revision).append(LINEBREAK).toString();
            }
            return "";
        }

        private Object get(String path, Map<String, Object> section)
        {
            if (section == null || section.isEmpty())
            {
                return null;
            }
            if (path.contains("."))
            {
                return this.get(this.getSubPath(path), (Map<String, Object>)section.get(this.findKey(this.getBasePath(path))));
            }
            return section.get(this.findKey(path));
        }

        private void set(String path, Object value, Map<String, Object> section)
        {
            if (path.contains("."))
            {
                Map<String, Object> subsection = this.getOrCreateSubSection(this.getBasePath(path), section);
                this.set(this.getSubPath(path), value, subsection);
            }
            else
            {
                section.put(path, value);
            }
        }

        private Map<String, Object> getOrCreateSubSection(String path, Map<String, Object> basesection)
        {
            if (path.contains("."))
            {
                Map<String, Object> subsection = this.getOrCreateSubSection(this.getBasePath(path), basesection);
                basesection.put(this.getBasePath(path), subsection);
                return this.getOrCreateSubSection(this.getSubPath(path), subsection);
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

        private String findKey(String key)
        {
            String foundKey = this.loadedKeys.get(key);
            if (foundKey == null)
            {
                return key;
            }
            return foundKey;
        }

        private void loadKeys(Map<String, Object> section)
        {
            for (String key : section.keySet())
            {
                this.loadedKeys.put(key.toLowerCase(Locale.ENGLISH), key);
                if (section.get(key) instanceof Map)
                {
                    this.loadKeys((Map<String, Object>)section.get(key));
                }
            }
        }

        public String getSubPath(String path)
        {
            return path.substring(path.indexOf('.') + 1);
        }

        public String getBasePath(String path)
        {
            return path.substring(0, path.indexOf('.'));
        }

        private void addComment(String path, String comment)
        {
            this.comments.put(path.toLowerCase(Locale.ENGLISH), comment);
        }

        public String getComment(String path)
        {
            return this.comments.get(path);
        }

        public Map<String, Object> fillFromFields(Configuration config, String basePath, Map<String, Object> section) throws IllegalArgumentException, ConversionException, IllegalAccessException
        {
            this.config = config;
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
            for (Field field : clazz.getFields())
            {
                if (field.isAnnotationPresent(Option.class))
                {
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
                    this.set(path.toLowerCase(Locale.ENGLISH), this.convertFromFieldValueToObject(field, field.get(config), path, this.getOrCreateSubSection(path, section)), section);
                }
            }
            return section;
        }

        public Object convertFromFieldValueToObject(Field field, Object fieldValue, String path, Map<String, Object> section) throws ConversionException, IllegalArgumentException, IllegalAccessException
        {
            if (fieldValue == null)
            {
                return null;
            }
            Class fieldClass = fieldValue.getClass();
            if (!String.class.isAssignableFrom(fieldClass))
            {
                if (fieldValue instanceof Configuration)
                {
                    return this.fillFromFields((Configuration)fieldValue, path, this.getOrCreateSubSection(path, section));
                }
                Converter converter = Convert.matchConverter(fieldClass);
                if (converter == null)
                {
                    Class genericType = field.getAnnotation(Option.class).genericType();
                    GenericConverter gConverter = Convert.matchGenericConverter(fieldClass);
                    if (Configuration.class.isAssignableFrom(genericType))
                    {
                        if (gConverter instanceof MapConverter)
                        {
                            Map<Object, ? extends Configuration> fieldMap = (Map)fieldValue;
                            Map<String, Object> map = new LinkedHashMap<String, Object>();
                            for (Object key : fieldMap.keySet())
                            {
                                map.put(key.toString(), new CodecContainer().fillFromFields(fieldMap.get(key), key.toString(), this.getOrCreateSubSection(key.toString(), map)));
                            }
                            return map;
                        }
                        else
                        {
                            throw new InvalidConfigurationException("Configurations can not load inside an array or collection!");
                        }
                    }
                    if (gConverter != null)
                    {
                        return gConverter.toObject(fieldValue, genericType, null);
                    }
                }
                else
                {
                    return converter.toObject(fieldValue);
                }
            }
            return fieldValue;

        }
    }
}
