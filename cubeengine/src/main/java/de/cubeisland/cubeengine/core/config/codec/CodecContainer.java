package de.cubeisland.cubeengine.core.config.codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.NullNode;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.Convert;

import gnu.trove.map.hash.THashMap;

/**
 * This class temporarily holds the values/comments of the configuration to
 * save or load them.
 */
public class CodecContainer<ConfigCodec extends ConfigurationCodec>
{
    public MapNode values;
    public Map<String, String> comments;
    public Integer revision = null;
    public CodecContainer superContainer = null; // used if given config is a SubConfig
    private String parentPath;

    protected ConfigCodec codec;

    /**
     * Container for normal Configuration
     */
    public CodecContainer(ConfigCodec codec)
    {
        this.comments = new THashMap<String, String>();
        this.codec = codec;
    }

    /**
     * Container for saving subConfigs
     *
     * @param superContainer
     */
    public CodecContainer(CodecContainer<ConfigCodec> superContainer, String parentPath)
    {
        this.codec = superContainer.codec;
        this.superContainer = superContainer;
        this.parentPath = parentPath;
    }

    /**
     * Fills the map with values form the inputStream
     *
     * @param is an InputStream
     */
    public void fillFromInputStream(InputStream is)
    {
        codec.loadFromInputStream(this, is);
    }

    protected static final int NORMAL_FIELD = 0;
    protected static final int CONFIG_FIELD = 1;
    protected static final int COLLECTION_CONFIG_FIELD = 2;
    protected static final int MAP_CONFIG_FIELD = 3;

    protected boolean isConfigField(Field field)
    {
        int mask = field.getModifiers();
        if ((((mask & Modifier.STATIC) == Modifier.STATIC))) // skip static fields
        {
            return false;
        }
        if (field.isAnnotationPresent(Option.class))
        {
            return true;
        }
        return false;
    }

    protected int getFieldType(Field field)
    {
        int fieldType = NORMAL_FIELD;
        if (Configuration.class.isAssignableFrom(field.getType()))
        {
            return CONFIG_FIELD;
        }
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pType = (ParameterizedType)type;
            if (Collection.class.isAssignableFrom((Class)pType.getRawType()))
            {
                Type subType1 = pType.getActualTypeArguments()[0];
                if (subType1 instanceof Class && Configuration.class.isAssignableFrom((Class)subType1))
                {
                    return COLLECTION_CONFIG_FIELD;
                }
            }

            if (Map.class.isAssignableFrom((Class)pType.getRawType()))
            {
                Type subType2 = pType.getActualTypeArguments()[1];
                if (subType2 instanceof Class && Configuration.class.isAssignableFrom((Class)subType2))
                {
                    return MAP_CONFIG_FIELD;
                }
            }
        }
        return fieldType;
    }

    protected void dumpIntoFields(Configuration config, MapNode currentNode)
    {
        for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
        {
            try
            {
                if (this.isConfigField(field))
                {
                    String path = field.getAnnotation(Option.class).value().replace(".", codec.PATH_SEPARATOR);
                    Node fieldNode = currentNode.getNodeAt(path, codec.PATH_SEPARATOR);
                    Type type = field.getGenericType();
                    int fieldType = this.getFieldType(field);
                    switch (fieldType)
                    {
                        case NORMAL_FIELD:
                            if (!(fieldNode instanceof NullNode))
                            {
                                Object object = Convert.fromNode(fieldNode, type); // Convert the value
                                if (object != null)
                                {
                                    field.set(config, object);//Set loaded Value into Field
                                }
                            }
                            continue;
                        case CONFIG_FIELD:
                            MapNode loadFrom_singleConfig;
                            Configuration singleSubConfig = (Configuration)field.get(config); // Get Config from field
                            if (singleSubConfig == null)
                            {
                                singleSubConfig = (Configuration)field.getType().newInstance(); // create new if null
                                field.set(config, singleSubConfig); // Set new instance
                            }
                            if (fieldNode instanceof MapNode)
                            {
                                loadFrom_singleConfig = (MapNode)fieldNode;
                            }
                            else if (fieldNode instanceof NullNode) // Empty Node
                            {
                                loadFrom_singleConfig = MapNode.emptyMap(); // Create Empty Map
                                currentNode.setNodeAt(path, codec.PATH_SEPARATOR, loadFrom_singleConfig); // and attach
                            }
                            else
                            {
                                throw new InvalidConfigurationException("Invalid Node for Configuration at " + path +
                                        "\nConfig:" + config.getClass() +
                                        "\nSubConfig:" + singleSubConfig.getClass());
                            }
                            new CodecContainer(codec).dumpIntoFields(singleSubConfig, loadFrom_singleConfig);
                            continue;
                        case COLLECTION_CONFIG_FIELD:
                            ListNode loadFrom_List;
                            if (fieldNode instanceof ListNode)
                            {
                                loadFrom_List = (ListNode)fieldNode;
                            }
                            else if (fieldNode instanceof NullNode) // Empty Node
                            {
                                loadFrom_List = ListNode.emptyList(); // Create Empty List
                                currentNode.setNodeAt(path, codec.PATH_SEPARATOR, loadFrom_List); // and attach
                            }
                            else
                            {
                                throw new InvalidConfigurationException("Invalid Node for List-Configurations at " + path +
                                        "\nConfig:" + config.getClass());
                            }
                            Collection<Configuration> subConfigs = (Collection<Configuration>)field.get(config);;
                            // Now iterate through the subConfigs
                            Iterator<Node> loadFrom_Iterator = loadFrom_List.getListedNodes().iterator();
                            for (Configuration subConfig : subConfigs)
                            {
                                Node loadFrom_listElem;
                                if (loadFrom_Iterator.hasNext())
                                {
                                    loadFrom_listElem = loadFrom_Iterator.next();
                                    if (loadFrom_listElem instanceof NullNode)
                                    {
                                        loadFrom_listElem = MapNode.emptyMap();
                                        loadFrom_List.addNode(loadFrom_listElem);
                                    }
                                }
                                else
                                {
                                    loadFrom_listElem = MapNode.emptyMap();
                                    loadFrom_List.addNode(loadFrom_listElem);
                                }
                                if (loadFrom_listElem instanceof MapNode)
                                {
                                    new CodecContainer(codec).dumpIntoFields(subConfig, (MapNode)loadFrom_listElem);
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Node for List-Configurations at " + path +
                                            "\nConfig:" + config.getClass() +
                                            "\nSubConfig:" + subConfig.getClass());
                                }
                            }
                            continue;
                        case MAP_CONFIG_FIELD:
                            MapNode loadFrom_Map;
                            if (fieldNode instanceof MapNode)
                            {
                                loadFrom_Map = (MapNode)fieldNode;
                            }
                            else if (fieldNode instanceof NullNode) // Empty Node
                            {
                                loadFrom_Map = MapNode.emptyMap(); // Create Empty List
                                currentNode.setNodeAt(path, codec.PATH_SEPARATOR, loadFrom_Map); // and attach
                            }
                            else
                            {
                                throw new InvalidConfigurationException("Invalid Node for Map-Configurations at " + path +
                                        "\nConfig:" + config.getClass());
                            }
                            Map<Object, Configuration> mapConfigs = (Map<Object, Configuration>)field.get(config);
                            for (Map.Entry<Object, Configuration> entry : mapConfigs.entrySet())
                            {
                                Node keyNode = Convert.toNode(entry.getKey());
                                if (keyNode instanceof StringNode)
                                {
                                    Node valueNode = loadFrom_Map.getNodeAt(((StringNode)keyNode).getValue(), codec.PATH_SEPARATOR);
                                    if (valueNode instanceof NullNode)
                                    {
                                        valueNode = MapNode.emptyMap();
                                        loadFrom_Map.setNode((StringNode)keyNode, valueNode);
                                    }
                                    if (valueNode instanceof MapNode)
                                    {
                                        new CodecContainer(codec).dumpIntoFields(entry.getValue(), (MapNode)valueNode);
                                    }
                                    else
                                    {
                                        throw new InvalidConfigurationException("Invalid Value-Node for Map of Configuration at " + path +
                                                "\nConfig:" + config.getClass() +
                                                "\nSubConfig:" + entry.getValue().getClass());
                                    }
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at " + path +
                                            "\nConfig:" + config.getClass() +
                                            "\nSubConfig:" + entry.getValue().getClass());
                                }
                            }
                            continue;
                    }
                }
            }
            catch (Exception e)
            {
                throw new InvalidConfigurationException(
                        "Error while dumping loaded config into fields!" +
                                "\ncurrent config: " + config.getClass().toString() +
                                "\ncurrent field:" + field.getName(), e);
            }
        }
    }

    /**
     * Saves the values into a file
     *
     * @param file the file to save to
     * @throws java.io.IOException
     */
    protected void saveIntoFile(Configuration config, File file) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        writer.append(this.dumpIntoString(config));
        writer.flush();
        writer.close();
    }

    /**
     * Converts the values into a String to save
     *
     * @return the converted map
     */
    private String dumpIntoString(Configuration config)
    {
        StringBuilder sb = new StringBuilder();
        if (config.head() != null)
        {
            sb.append("# ").append(StringUtils.implode("\n# ", config.head())).append(codec.LINE_BREAK).append(codec.LINE_BREAK);
        }
        codec.first = true;
        sb.append(codec.convertMap(this, values, 0, false).trim()).append("\n");
        if (config.tail() != null)
        {
            sb.append("# ").append(StringUtils.implode("\n# ", config.tail()));
        }
        return sb.toString();
    }

    /**
     * Adds a comment to be saved
     *
     * @param commentPath the commentPath
     * @param comment the comment
     */
    protected void addComment(String commentPath, String comment)
    {
        if (superContainer == null)
        {
            this.comments.put(commentPath.toLowerCase(), comment);
        }
        else
        {
            superContainer.addComment(this.parentPath + codec.PATH_SEPARATOR + commentPath, comment);
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
     * @param config the config
     * @param baseNode the baseNode to fill
     */
    public <C extends Configuration> void fillFromFields(C config, MapNode baseNode)
    {
        Class<C> configClass = (Class<C>) config.getClass();
        if (configClass.isAnnotationPresent(MapComments.class))
        {
            MapComment[] mapComments = configClass.getAnnotation(MapComments.class).value();
            for (MapComment comment : mapComments)
            {
                this.addComment(comment.path().replace(".", codec.PATH_SEPARATOR), comment.text());
            }
        }
        boolean advanced = true;
        try
        // to get a boolean advanced field (if not found ignore)
        {
            Field field = configClass.getField("advanced");
            advanced = field.getBoolean(config);
        }
        catch (Exception ignored)
        {}
        for (Field field : configClass.getFields())
        {
            try
            {
                if (this.isConfigField(field))
                {
                    if (!advanced && field.getAnnotation(Option.class).advanced())
                    {
                        continue;
                    }
                    Object fieldValue = field.get(config);
                    String path = field.getAnnotation(Option.class).value().replace(".", codec.PATH_SEPARATOR);
                    if (field.isAnnotationPresent(Comment.class))
                    {
                        Comment comment = field.getAnnotation(Comment.class);
                        this.addComment(path, comment.value());
                    }
                    int fieldType = this.getFieldType(field);
                    switch (fieldType)
                    {
                        case NORMAL_FIELD:
                            Node fieldValueNode = Convert.toNode(fieldValue);
                            baseNode.setNodeAt(path, codec.PATH_SEPARATOR, fieldValueNode);
                            continue;
                        case CONFIG_FIELD:
                            CodecContainer subContainer = new CodecContainer(this, path); // Create new container
                            subContainer.fillFromFields((Configuration)fieldValue,
                                    (MapNode)baseNode.getNodeAt(path, codec.PATH_SEPARATOR));
                            continue;
                        case COLLECTION_CONFIG_FIELD:
                            ListNode listNode = ListNode.emptyList();
                            baseNode.setNodeAt(path, codec.PATH_SEPARATOR, listNode);
                            int pos = 0;
                            for (Configuration subConfig : (Collection<Configuration>)fieldValue)
                            {
                                MapNode configNode = MapNode.emptyMap();
                                listNode.addNode(configNode);
                                new CodecContainer(this, path + codec.PATH_SEPARATOR + "[" + pos++).fillFromFields(subConfig, configNode);
                            }
                            continue;
                        case MAP_CONFIG_FIELD:
                            MapNode mapNode = MapNode.emptyMap();
                            baseNode.setNodeAt(path, codec.PATH_SEPARATOR, mapNode);
                            Map<Object, Configuration> fieldMap = (Map<Object, Configuration>)fieldValue;
                            for (Map.Entry<Object, Configuration> entry : fieldMap.entrySet())
                            {
                                Node keyNode = Convert.toNode(entry.getKey());
                                if (keyNode instanceof StringNode)
                                {
                                    MapNode configNode = MapNode.emptyMap();
                                    mapNode.setNode((StringNode)keyNode, configNode);
                                    new CodecContainer(this, path + codec.PATH_SEPARATOR + ((StringNode)keyNode).getValue())
                                            .fillFromFields(entry.getValue(), configNode);
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at " + path +
                                            "\nConfig:" + config.getClass());
                                }
                            }
                            continue;
                    }
                }
            }
            catch (Exception e)
            {
                throw new InvalidConfigurationException(
                        "Error while dumping loaded config into fields!" +
                                "\ncurrent config: " + config.getClass().toString() +
                                "\ncurent field:" + field.getName(), e);
            }
        }
    }
}