package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.annotations.*;
import de.cubeisland.cubeengine.core.config.node.*;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.converter.generic.CollectionConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.MapConverter;
import gnu.trove.map.hash.THashMap;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * This abstract Codec can be implemented to read and write configurations.
 */
public abstract class ConfigurationCodec
{
    protected String COMMENT_PREFIX;
    protected String OFFSET;
    protected String LINE_BREAK;
    protected String QUOTE;
    protected final String PATH_SEPARATOR = ":";
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
            if (config.getClass().isAnnotationPresent(Updater.class))
            {
                if (a_revision.value() > container.revision)
                {
                    Updater updater = config.getClass().getAnnotation(Updater.class);
                    updater.value().newInstance().update(container.values, container.revision);
                }
            }
        }
        container.dumpIntoFields(config, container.values, config.parent);
    }

     /**
     * Returns the offset as String
     *
     * @param offset the offset
     * @return the offset
     */
    protected String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(OFFSET);
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
            container.values = MapNode.emptyMap();
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                container.values.setNodeAt("revision", PATH_SEPARATOR, new IntNode(a_revision.value()));
            }
            container.fillFromFields(null, config, container.values);
            container.saveIntoFile(config, file);
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
            container.values = MapNode.emptyMap();
            if (a_revision != null)
            {
                container.values.setNodeAt("revision", PATH_SEPARATOR, new IntNode(a_revision.value()));
            }
            container.fillFromFields(parentConfig, config, container.values);
            container.saveIntoFile(config, file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
    }

    /**
     * Serializes the values in the map
     *
     * @param container the codec-container
     * @param values the values at given path
     * @param off the current offset
     * @param inCollection
     * @return  the serialized value
     */
    public abstract String convertMap(CodecContainer container, MapNode values, int off, boolean inCollection);

    /**
     * Serializes a single value
     *
     * @param container the codec-container
     * @param value the value at given path
     * @param off the current offset
     * @param inCollection
     * @return
     */
    public abstract String convertValue(CodecContainer container, Node value, int off, boolean inCollection);

    /**
     * Builds a the comment for given path
     *
     * @param path the path
     * @param off the current offset
     * @return the comment
     */
    public abstract String buildComment(CodecContainer container, String path, int off);

    /**
     * Returns the FileExtension as String
     *
     * @return the fileExtension
     */
    public abstract String getExtension();

    /**
     * Converts the inputStream into a readable Object
     * @param container the container to fill with values
     * @param is the InputStream
     */
    public abstract void loadFromInputStream(CodecContainer container, InputStream is);

    /**
     * This class temporarily holds the values/comments of the configuration to
     * save or load them.
     */
    public class CodecContainer
    {
        public MapNode values;
        public Map<String, String> comments;
        public Integer revision = null;
        public CodecContainer parentContainer = null; // used if given config is a SubConfig
        private String parentPath;

        /**
         * Container for normal Configuration
         */
        public CodecContainer()
        {
            this.comments = new THashMap<String, String>();
        }

        /**
         * Container for saving subConfigs
         *
         * @param parentContainer
         */
        public CodecContainer(CodecContainer parentContainer, String parentPath)
        {
            this.parentContainer = parentContainer;
            this.parentPath = parentPath;
        }

        /**
         * Fills the map with values form the inputStream
         *
         * @param is an InputStream
         */
        public void fillFromInputStream(InputStream is)
        {
            loadFromInputStream(this, is);
        }

        private static final int NORMAL_FIELD = 0;
        private static final int CONFIG_FIELD = 1;
        private static final int COLLECTION_CONFIG_FIELD = 2;
        private static final int MAP_CONFIG_FIELD = 3;

        /**
         * Dumps the values from given Node into the fields of the Configuration
         *
         * @param config the config
         * @param currentNode the node to load from
         * @param parentConfig the optional parentConfig
         */
        private void dumpIntoFields(Configuration config, MapNode currentNode, Configuration parentConfig)
        {
            for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
            {
                try
                {
                    int mask = field.getModifiers();
                    if ((((mask & Modifier.STATIC) == Modifier.STATIC))) // skip static fields
                    {
                        continue;
                    }
                    if (field.isAnnotationPresent(Option.class))
                    {
                        String path = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                        Node fieldNode = currentNode.getNodeAt(path, PATH_SEPARATOR);
                        Type type = field.getGenericType();
                        int fieldType = NORMAL_FIELD;
                        if (Configuration.class.isAssignableFrom(field.getType()))
                        {
                            fieldType = CONFIG_FIELD;
                        }
                        else if (type instanceof ParameterizedType)
                        {
                            ParameterizedType pType = (ParameterizedType)type;
                            if (Collection.class.isAssignableFrom((Class)pType.getRawType()))
                            {
                                Type subType1 = pType.getActualTypeArguments()[0];
                                if (subType1 instanceof Class && Configuration.class.isAssignableFrom((Class)subType1))
                                {
                                    fieldType = COLLECTION_CONFIG_FIELD;
                                }
                            }
                            else if (Map.class.isAssignableFrom((Class)pType.getRawType()))
                            {
                                Type subType2 = pType.getActualTypeArguments()[1];
                                if (subType2 instanceof Class && Configuration.class.isAssignableFrom((Class)subType2))
                                {
                                    fieldType = MAP_CONFIG_FIELD;
                                }
                            }
                        }
                        switch (fieldType)
                        {
                            case NORMAL_FIELD:
                                if (fieldNode instanceof NullNode)
                                {
                                    if (parentConfig != null)
                                    {
                                        field.set(config, field.get(parentConfig));
                                    }
                                    continue;
                                }
                                Object object = Convert.fromNode(fieldNode, type); // Convert the value
                                if (object != null)
                                {
                                    field.set(config, object);//Set loaded Value into Field
                                }
                                else if (parentConfig != null) // If not loaded but is child-config get from parent-config
                                {
                                    field.set(config, field.get(parentConfig));
                                }
                                continue;
                            case CONFIG_FIELD:
                                MapNode loadFrom_singleConfig;
                                Configuration singleSubConfig = (Configuration)field.get(config); // Get Config from field
                                if (singleSubConfig == null)
                                {
                                    singleSubConfig = (Configuration)field.getType().newInstance(); // create new if null
                                    field.set(config,singleSubConfig); // Set new instance
                                }
                                if (fieldNode instanceof MapNode)
                                {
                                    loadFrom_singleConfig = (MapNode)fieldNode;
                                }
                                else if (fieldNode instanceof NullNode) // Empty Node
                                {
                                    loadFrom_singleConfig = MapNode.emptyMap(); // Create Empty Map
                                    currentNode.setNodeAt(path,PATH_SEPARATOR,loadFrom_singleConfig); // and attach
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Node for Configuration at " + path +
                                            "\nConfig:"+config.getClass()+
                                            "\nSubConfig:"+singleSubConfig.getClass());
                                }
                                new CodecContainer().dumpIntoFields(singleSubConfig, loadFrom_singleConfig,
                                    parentConfig == null ? null : (Configuration)field.get(parentConfig));
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
                                    currentNode.setNodeAt(path,PATH_SEPARATOR,loadFrom_List); // and attach
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Node for List-Configurations at " + path +
                                            "\nConfig:"+config.getClass());
                                }
                                Collection<Configuration> subConfigs;
                                Collection<Configuration> parentSubConfigs = null;
                                Iterator<Configuration> parentConfig_Iterator = null;
                                if (parentConfig == null) // Get configs from field
                                {
                                    subConfigs = (Collection<Configuration>) field.get(config);
                                }
                                else // get configs from parent & create new Collection with new instances
                                {
                                    parentSubConfigs = (Collection<Configuration>) field.get(parentConfig);
                                    subConfigs = CollectionConverter.getCollectionFor((ParameterizedType)type);
                                    for (Configuration configuration : parentSubConfigs)
                                    {
                                        subConfigs.add(configuration.getClass().newInstance());
                                    }
                                    field.set(config,subConfigs);
                                    parentConfig_Iterator = parentSubConfigs.iterator();
                                }
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
                                        if (parentConfig == null)
                                        {
                                            new CodecContainer().dumpIntoFields(subConfig, (MapNode)loadFrom_listElem, null);
                                        }
                                        else
                                        {
                                            new CodecContainer().dumpIntoFields(subConfig, (MapNode)loadFrom_listElem, parentConfig_Iterator.next());
                                        }
                                    }
                                    else
                                    {
                                        throw new InvalidConfigurationException("Invalid Node for List-Configurations at " + path +
                                                "\nConfig:"+config.getClass()+
                                                "\nSubConfig:"+subConfig.getClass());
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
                                    currentNode.setNodeAt(path,PATH_SEPARATOR,loadFrom_Map); // and attach
                                }
                                else
                                {
                                    throw new InvalidConfigurationException("Invalid Node for Map-Configurations at " + path +
                                            "\nConfig:"+config.getClass());
                                }
                                Map<Object,Configuration> mapConfigs;
                                Map<Object,Configuration> parentMapConfigs = null;
                                if (parentConfig == null)
                                {
                                    mapConfigs = (Map<Object,Configuration>) field.get(config);
                                }
                                else
                                {
                                    parentMapConfigs = (Map<Object,Configuration>) field.get(parentConfig);
                                    mapConfigs = MapConverter.getMapFor((ParameterizedType)type);
                                    for (Map.Entry<Object,Configuration> entry : parentMapConfigs.entrySet())
                                    {
                                        mapConfigs.put(entry.getKey(),entry.getValue().getClass().newInstance());
                                    }
                                    field.set(config,mapConfigs);
                                }
                                for (Map.Entry<Object,Configuration> entry : mapConfigs.entrySet())
                                {
                                    Node keyNode = Convert.toNode(entry.getKey());
                                    if (keyNode instanceof StringNode)
                                    {
                                        Node valueNode = loadFrom_Map.getNodeAt(((StringNode)keyNode).getValue(),PATH_SEPARATOR);
                                        if (valueNode instanceof NullNode)
                                        {
                                            valueNode = MapNode.emptyMap();
                                            loadFrom_Map.setNode((StringNode)keyNode,valueNode);
                                        }
                                        if (valueNode instanceof MapNode)
                                        {
                                            if (parentConfig == null)
                                            {
                                                new CodecContainer().dumpIntoFields(entry.getValue(),(MapNode)valueNode, null);
                                            }
                                            else
                                            {
                                                new CodecContainer().dumpIntoFields(entry.getValue(),(MapNode)valueNode,
                                                        parentMapConfigs.get(entry.getKey()));
                                            }
                                        }
                                        else
                                        {
                                            throw new InvalidConfigurationException("Invalid Value-Node for Map of Configuration at "+path +
                                                    "\nConfig:"+config.getClass()+
                                                    "\nSubConfig:"+entry.getValue().getClass());
                                        }
                                    }
                                    else
                                    {
                                        throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at "+path +
                                                "\nConfig:"+config.getClass()+
                                                "\nSubConfig:"+entry.getValue().getClass());
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
        private void saveIntoFile(Configuration config, File file) throws IOException
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
                sb.append("# ").append(StringUtils.implode("\n# ", config.head())).append(LINE_BREAK).append(LINE_BREAK);
            }
            first = true;
            sb.append(convertMap(this, values, 0, false).trim()).append("\n");
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
        private void addComment(String commentPath, String comment)
        {
            if (parentContainer == null)
            {
                this.comments.put(commentPath.toLowerCase(), comment);
            }
            else
            {
                parentContainer.addComment(this.parentPath +PATH_SEPARATOR+ commentPath, comment);
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
         * ills the map with values from the Fields to save
         *
         * @param parentConfig the parent config
         * @param config the config
         * @param baseNode the baseNode to fill
         */
        public void fillFromFields(Configuration parentConfig, Configuration config, MapNode baseNode)
        {
            if (parentConfig != null)
            {
                if (!parentConfig.getClass().equals(config.getClass()))
                {
                    throw new IllegalStateException("parent and child-config have to be the same type of config!");
                }
            }
            Class<? extends Configuration> configClass = config.getClass();
            if (configClass.isAnnotationPresent(MapComments.class))
            {
                MapComment[] mapComments = configClass.getAnnotation(MapComments.class).value();
                for (MapComment comment : mapComments)
                {
                    this.addComment(comment.path().replace(".", PATH_SEPARATOR), comment.text());
                }
            }
            boolean advanced = true;
            try // to get a boolean advanced field (if not found ignore)
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
                        Object fieldValue = field.get(config);
                        String path = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                        if (field.isAnnotationPresent(Comment.class))
                        {
                            Comment comment = field.getAnnotation(Comment.class);
                            this.addComment(path, comment.value());
                        }
                        // First check for possible SubConfigurations...
                        if (Configuration.class.isAssignableFrom(field.getType())) // SingleSubConfig
                        {
                            CodecContainer subContainer = new CodecContainer(this, path); // Create new container
                            if (parentConfig == null)
                            {
                                subContainer.fillFromFields(null,(Configuration)fieldValue,
                                                                 (MapNode)baseNode.getNodeAt(path,PATH_SEPARATOR));
                            }
                            else
                            {
                                subContainer.fillFromFields((Configuration)field.get(parentConfig),
                                                            (Configuration)fieldValue,
                                                            (MapNode)baseNode.getNodeAt(path,PATH_SEPARATOR));
                            }
                        }
                        else if (field.getGenericType() instanceof ParameterizedType) // Next check for possible SubConfiguration in Maps/Collections...
                        {
                            ParameterizedType pType = (ParameterizedType)field.getGenericType();
                            if (Collection.class.isAssignableFrom((Class)pType.getRawType()))
                            {
                                Type subType1 = pType.getActualTypeArguments()[0];
                                if (subType1  instanceof  Class && Configuration.class.isAssignableFrom((Class)subType1)) // is Collection of Configuration
                                {
                                    ListNode listNode = ListNode.emptyList();
                                    baseNode.setNodeAt(path,PATH_SEPARATOR,listNode);
                                    if (parentConfig == null) // No parent given: iterate through given configs & load them
                                    {
                                        int pos = 0;
                                        for (Configuration subConfig : (Collection<Configuration>) fieldValue)
                                        {
                                            MapNode configNode = MapNode.emptyMap();
                                            listNode.addNode(configNode);
                                            new CodecContainer(this, path + PATH_SEPARATOR + "["+pos++).fillFromFields(null,subConfig,configNode);
                                        }
                                    }
                                    else // Parent given: NOT ALLOWED
                                    {
                                        throw new InvalidConfigurationException("ChildConfigs are not allowed for Configurations in Collections"+
                                                "\nConfig:"+config.getClass());
                                    }
                                    continue;
                                }
                            }
                            else if (Map.class.isAssignableFrom((Class)pType.getRawType()))
                            {
                                Type subType2 = pType.getActualTypeArguments()[1];
                                if (subType2 instanceof Class && Configuration.class.isAssignableFrom((Class)subType2))  // is Map of Configuration
                                {
                                    MapNode mapNode = MapNode.emptyMap();
                                    baseNode.setNodeAt(path,PATH_SEPARATOR,mapNode);
                                    if (parentConfig == null) // no parent: iterate through existing Configurations and load them
                                    {
                                        Map<Object,Configuration> fieldMap = (Map<Object,Configuration>) fieldValue;
                                        for (Map.Entry<Object,Configuration> entry : fieldMap.entrySet())
                                        {
                                            Node keyNode = Convert.toNode(entry.getKey());
                                            if (keyNode instanceof StringNode)
                                            {
                                                MapNode configNode = MapNode.emptyMap();
                                                mapNode.setNode((StringNode) keyNode, configNode);
                                                new CodecContainer(this,path + PATH_SEPARATOR + ((StringNode) keyNode).getValue())
                                                        .fillFromFields(null, entry.getValue(), configNode);
                                            }
                                            else
                                            {
                                                throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at "+path +
                                                        "\nConfig:"+config.getClass());
                                            }
                                        }
                                    }
                                    else // parent not null: iterate parentConfig and override child with new values
                                    {
                                        Map<Object,Configuration> parentFieldMap = (Map<Object,Configuration>) field.get(parentConfig);
                                        Map<Object,Configuration> childFieldMap = MapConverter.getMapFor(pType);
                                        for (Map.Entry<Object,Configuration> parentEntry : parentFieldMap.entrySet())
                                        {
                                            Node keyNode = Convert.toNode(parentEntry.getKey());
                                            if (keyNode instanceof StringNode)
                                            {
                                                MapNode configNode = MapNode.emptyMap();
                                                mapNode.setNode((StringNode) keyNode, configNode);
                                                new CodecContainer(this,path + PATH_SEPARATOR +((StringNode) keyNode).getValue())
                                                        .fillFromFields(parentEntry.getValue(), childFieldMap.get(parentEntry.getKey()), configNode);
                                            }
                                            else
                                            {
                                                throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at "+path +
                                                        "\nConfig:"+config.getClass());
                                            }
                                        }
                                    }
                                    continue;
                                }
                            }
                        }
                        // else not Collection or Map (Arrays of Configurations are not allowed)
                        if (parentConfig != null)
                        {
                            Object parentValue = field.get(parentConfig);
                            if (parentValue.equals(fieldValue))
                            {
                                baseNode.removeNode(path, PATH_SEPARATOR);
                                return;
                            }
                        }
                        Node fieldValueNode = Convert.toNode(fieldValue);
                        baseNode.setNodeAt(path,PATH_SEPARATOR,fieldValueNode);
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
    }
}
