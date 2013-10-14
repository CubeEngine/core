/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.config.codec;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.MapComment;
import de.cubeisland.engine.core.config.annotations.MapComments;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.Convert;
import de.cubeisland.engine.core.util.convert.converter.generic.MapConverter;

/**
 * This abstract Codec can be implemented to read and write configurations.
 */
public abstract class ConfigurationCodec
{
    protected static final String PATH_SEPARATOR = ":";

    /**
     * Loads in the given configuration using the InputStream
     *
     * @param config the config to load
     * @param is the InputStream to load from
     */
    public void load(Configuration config, InputStream is)
    {
        this.dumpIntoFields(config, this.loadFromInputStream(is));
    }

    /**
     * Saves the configuration into given file
     *
     * @param config the configuration to save
     * @param file the file to save into
     */
    public void save(Configuration config, Path file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            this.saveIntoFile(config, this.fillFromFields(config), file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
    }

    protected abstract void saveIntoFile(Configuration config, MapNode node, Path file) throws IOException;

    /**
     * Converts the inputStream into a readable Object
     * @param is the InputStream
     */
    public abstract MapNode loadFromInputStream(InputStream is);

    /**
     * Returns the FileExtension as String
     *
     * @return the fileExtension
     */
    public abstract String getExtension();

    protected void doFieldComment(Node node, Field field)
    {
        if (field.isAnnotationPresent(Comment.class))
        {
            Comment comment = field.getAnnotation(Comment.class);
            node.setComment(comment.value());
        }
    }

    protected void doMapComments(MapNode baseNode, Class<? extends Configuration> configClass)
    {
        if (configClass.isAnnotationPresent(MapComments.class))
        {
            MapComment[] mapComments = configClass.getAnnotation(MapComments.class).value();
            for (MapComment comment : mapComments)
            {
                Node nodeAt = baseNode.getNodeAt(comment.path(), ".");
                if (nodeAt != null) // if null ignore comment
                {
                    if (nodeAt instanceof NullNode)
                    {
                        nodeAt.getParentNode().removeNode(nodeAt);
                    }
                    else
                    {
                        nodeAt.setComment(comment.text());
                    }
                }
            }
        }
    }

    protected static final int NORMAL_FIELD = 0;
    protected static final int CONFIG_FIELD = 1;
    protected static final int COLLECTION_CONFIG_FIELD = 2;
    protected static final int MAP_CONFIG_FIELD = 3;

    protected static int getFieldType(Field field)
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

    protected static boolean isConfigField(Field field)
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

    protected void dumpIntoFields(Configuration config, MapNode currentNode)
    {
        for (Field field : config.getClass().getFields()) // ONLY public fields are allowed
        {
            try
            {
                if (isConfigField(field))
                {
                    String path = field.getAnnotation(Option.class).value().replace(".", PATH_SEPARATOR);
                    Node fieldNode = currentNode.getNodeAt(path, PATH_SEPARATOR);
                    Type type = field.getGenericType();
                    int fieldType = getFieldType(field);
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
                        MapNode singleConfigNode;
                        Configuration singleSubConfig = (Configuration)field.get(config); // Get Config from field
                        if (singleSubConfig == null)
                        {
                            singleSubConfig = (Configuration)field.getType().newInstance(); // create new if null
                            field.set(config, singleSubConfig); // Set new instance
                        }
                        if (fieldNode instanceof MapNode)
                        {
                            singleConfigNode = (MapNode)fieldNode;
                        }
                        else if (fieldNode instanceof NullNode) // Empty Node
                        {
                            singleConfigNode = MapNode.emptyMap(); // Create Empty Map
                            currentNode.setNodeAt(path, PATH_SEPARATOR, singleConfigNode); // and attach
                        }
                        else
                        {
                            throw new InvalidConfigurationException("Invalid Node for Configuration at " + path +
                                                                        "\nConfig:" + config.getClass() +
                                                                        "\nSubConfig:" + singleSubConfig.getClass());
                        }
                        this.dumpIntoFields(singleSubConfig, singleConfigNode);
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
                            currentNode.setNodeAt(path, PATH_SEPARATOR, loadFrom_List); // and attach
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
                            Node listElemNode;
                            if (loadFrom_Iterator.hasNext())
                            {
                                listElemNode = loadFrom_Iterator.next();
                                if (listElemNode instanceof NullNode)
                                {
                                    listElemNode = MapNode.emptyMap();
                                    loadFrom_List.addNode(listElemNode);
                                }
                            }
                            else
                            {
                                listElemNode = MapNode.emptyMap();
                                loadFrom_List.addNode(listElemNode);
                            }
                            if (listElemNode instanceof MapNode)
                            {
                                this.dumpIntoFields(subConfig, (MapNode)listElemNode);
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
                            currentNode.setNodeAt(path, PATH_SEPARATOR, loadFrom_Map); // and attach
                        }
                        else
                        {
                            throw new InvalidConfigurationException("Invalid Node for Map-Configurations at " + path +
                                                                        "\nConfig:" + config.getClass());
                        }
                        Class<? extends Configuration> clazz = (Class<? extends Configuration>)((ParameterizedType)type).getActualTypeArguments()[1];
                        Map<Object, Configuration> configs = MapConverter.getMapFor((ParameterizedType)type);
                        for (Entry<String, Node> entry : loadFrom_Map.getMappedNodes().entrySet())
                        {
                            Node keyNode = Convert.toNode(entry.getKey());
                            Node valueNode = loadFrom_Map.getNodeAt(entry.getKey(), this.PATH_SEPARATOR);
                            Object key = Convert.fromNode(keyNode,((ParameterizedType)type).getActualTypeArguments()[0]);
                            Configuration value;
                            if (valueNode instanceof NullNode)
                            {
                                value = clazz.newInstance();

                            }
                            else if (valueNode instanceof MapNode)
                            {
                                this.dumpIntoFields(value = clazz.newInstance(), (MapNode)valueNode);
                            }
                            else
                            {
                                throw new InvalidConfigurationException("Invalid Value-Node for Map of Configuration at " + path +
                                                                            "\nConfig:" + config.getClass() +
                                                                            "\nSubConfig:" + clazz);
                            }
                            configs.put(key,value);
                            field.set(config,configs);
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
                        "\ncurrent field:" + field.getName() +
                        "\ncurrent node: " + currentNode.toString() , e);
            }
        }
    }



    /**
     * Fills the map with values from the Fields to save
     *
     * @param config the config
     */
    public <C extends Configuration> MapNode fillFromFields(C config)
    {
        MapNode baseNode = MapNode.emptyMap();
        Class<C> configClass = (Class<C>) config.getClass();

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
            if (isConfigField(field))
            {
                if (!advanced && field.getAnnotation(Option.class).advanced())
                {
                    continue;
                }
                String path = field.getAnnotation(Option.class).value().replace(".", this.PATH_SEPARATOR);
                this.fillFromField(field,config,baseNode,path);
            }
        }
        this.doMapComments(baseNode, config.getClass());
        return baseNode;
    }

    protected void fillFromField(Field field, Configuration config, MapNode baseNode, String path)
    {
        try
        {
            Object fieldValue = field.get(config);
            int fieldType = getFieldType(field);
            switch (fieldType)
            {
            case NORMAL_FIELD:
                Node fieldValueNode = Convert.toNode(fieldValue);
                this.doFieldComment(fieldValueNode, field);
                baseNode.setNodeAt(path, PATH_SEPARATOR, fieldValueNode);
                return;
            case CONFIG_FIELD:

                MapNode singleConfigNode = this.fillFromFields((Configuration)fieldValue);
                baseNode.setNodeAt(path, PATH_SEPARATOR, singleConfigNode);
                this.doFieldComment(singleConfigNode, field);
                return;
            case COLLECTION_CONFIG_FIELD:
                ListNode listNode = ListNode.emptyList();
                baseNode.setNodeAt(path, PATH_SEPARATOR, listNode);
                int pos = 0;
                for (Configuration subConfig : (Collection<Configuration>)fieldValue)
                {
                    MapNode collectionConfigNode = this.fillFromFields(subConfig);
                    this.doFieldComment(collectionConfigNode, field);
                    listNode.addNode(collectionConfigNode);
                }
                return;
            case MAP_CONFIG_FIELD:
                MapNode mapNode = MapNode.emptyMap();
                this.doFieldComment(mapNode, field);
                baseNode.setNodeAt(path, PATH_SEPARATOR, mapNode);
                Map<Object, Configuration> fieldMap = (Map<Object, Configuration>)fieldValue;
                for (Map.Entry<Object, Configuration> entry : fieldMap.entrySet())
                {
                    Node keyNode = Convert.toNode(entry.getKey());
                    if (keyNode instanceof StringNode)
                    {
                        MapNode mapConfigNode = this.fillFromFields(entry.getValue());
                        mapNode.setNode((StringNode)keyNode, mapConfigNode);
                    }
                    else
                    {
                        throw new InvalidConfigurationException("Invalid Key-Node for Map of Configuration at " + path +
                                                                    "\nConfig:" + config.getClass());
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw InvalidConfigurationException.of(
                "Error while dumping loaded config into fields!" ,
                config.getPath(), path, config.getClass(), field, e);
        }
    }

}
