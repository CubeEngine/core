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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.MultiConfiguration;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.Convert;
import de.cubeisland.engine.core.util.convert.converter.generic.CollectionConverter;
import de.cubeisland.engine.core.util.convert.converter.generic.MapConverter;

public abstract class MultiCodecContainer<Codec extends MultiConfigurationCodec> extends CodecContainer<Codec>
{
    public MultiCodecContainer(Codec codec) {
        super(codec);
    }

    public MultiCodecContainer(MultiCodecContainer superContainer, String parentPath) {
        super(superContainer, parentPath);
    }

    /**
     * Dumps the values from given Node into the fields of the Configuration
     *
     * @param config the config
     * @param currentNode the node to load from
     * @param parentConfig the optional parentConfig
     */
    protected void dumpIntoFields(MultiConfiguration config, MapNode currentNode, MultiConfiguration parentConfig)
    {
        if (parentConfig == null)
        {
            this.dumpIntoFields(config,currentNode);
        }
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
                            if (fieldNode instanceof NullNode)
                            {
                                field.set(config, field.get(parentConfig));
                                config.addinheritedField(field);
                            }
                            else
                            {
                                Object object = Convert.fromNode(fieldNode, type); // Convert the value
                                if (object != null)
                                {
                                    field.set(config, object);//Set loaded Value into Field
                                }
                                else // If not loaded but is child-config get from parent-config
                                {
                                    field.set(config, field.get(parentConfig));
                                    config.addinheritedField(field);
                                }
                            }
                            continue;
                        case CONFIG_FIELD:
                            MapNode loadFrom_singleConfig;
                            MultiConfiguration singleSubConfig = (MultiConfiguration)field.get(config); // Get Config from field
                            if (singleSubConfig == null)
                            {
                                singleSubConfig = (MultiConfiguration)field.getType().newInstance(); // create new if null
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
                            this.codec.createContainer().dumpIntoFields(singleSubConfig, loadFrom_singleConfig,(MultiConfiguration)field.get(parentConfig));
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
                            Collection<MultiConfiguration> parentSubConfigs = (Collection<MultiConfiguration>)field.get(parentConfig);
                            Collection<MultiConfiguration> subConfigs = CollectionConverter
                                .getCollectionFor((ParameterizedType)type);
                            for (MultiConfiguration configuration : parentSubConfigs)
                            {
                                subConfigs.add(configuration.getClass().newInstance());
                            }
                            field.set(config, subConfigs);
                            Iterator<MultiConfiguration> parentConfig_Iterator = parentSubConfigs.iterator();
                            // Now iterate through the subConfigs
                            Iterator<Node> loadFrom_Iterator = loadFrom_List.getListedNodes().iterator();
                            for (MultiConfiguration subConfig : subConfigs)
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
                                    this.codec.createContainer().dumpIntoFields(subConfig, (MapNode)loadFrom_listElem, parentConfig_Iterator.next());
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
                            Map<Object, MultiConfiguration> mapConfigs = MapConverter.getMapFor((ParameterizedType)type);
                            Map<Object, MultiConfiguration> parentMapConfigs = (Map<Object, MultiConfiguration>)field.get(parentConfig);
                            for (Map.Entry<Object, MultiConfiguration> entry : parentMapConfigs.entrySet())
                            {
                                mapConfigs.put(entry.getKey(), entry.getValue().getClass().newInstance());
                            }
                            field.set(config, mapConfigs);
                            for (Map.Entry<Object, MultiConfiguration> entry : mapConfigs.entrySet())
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
                                        this.codec.createContainer().dumpIntoFields(entry
                                                                                  .getValue(), (MapNode)valueNode, parentMapConfigs
                                                                                  .get(entry.getKey()));
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
     * Fills the map with values from the Fields to save
     *
     * @param parentConfig the parent config
     * @param config the config
     * @param baseNode the baseNode to fill
     */
    public <C extends MultiConfiguration> void fillFromFields(C parentConfig, C config, MapNode baseNode)
    {
        if (parentConfig != null)
        {
            if (!parentConfig.getClass().equals(config.getClass()))
            {
                throw new IllegalStateException("parent and child-config have to be the same type of config!");
            }
        }
        else
        {
            this.fillFromFields(config,baseNode);
            return;
        }
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
            if (config.isInheritedField(field))
                continue;
            if (this.isConfigField(field))
            {
                if (!advanced && field.getAnnotation(Option.class).advanced())
                {
                    continue;
                }
                String path = field.getAnnotation(Option.class).value().replace(".", codec.PATH_SEPARATOR);
                this.fillFromField(field,parentConfig,config,baseNode,path);
            }
        }
        baseNode.cleanUpEmptyNodes();
    }

    protected void fillFromField(Field field, Configuration parentConfig, Configuration config, MapNode baseNode, String path)
    {
        try
        {
            Object fieldValue = field.get(config);
            int fieldType = this.getFieldType(field);
            switch (fieldType)
            {
            case NORMAL_FIELD:
                Node fieldValueNode = Convert.toNode(fieldValue);
                baseNode.setNodeAt(path, codec.PATH_SEPARATOR, fieldValueNode);
                return;
            case CONFIG_FIELD:
                MultiCodecContainer subContainer = this.createSubContainer(path); // Create new container
                subContainer.fillFromFields((MultiConfiguration)field.get(parentConfig),
                                            (MultiConfiguration)fieldValue,
                                            (MapNode)baseNode.getNodeAt(path, codec.PATH_SEPARATOR));
                return;
            case COLLECTION_CONFIG_FIELD:
                throw new InvalidConfigurationException("ChildConfigs are not allowed for Configurations in Collections" +
                                                            "\nConfig:" + config.getClass());
            case MAP_CONFIG_FIELD:
                MapNode mapNode = MapNode.emptyMap();
                baseNode.setNodeAt(path, codec.PATH_SEPARATOR, mapNode);
                Map<Object, MultiConfiguration> parentFieldMap = (Map<Object, MultiConfiguration>)field.get(parentConfig);
                Map<Object, MultiConfiguration> childFieldMap = MapConverter.getMapFor((ParameterizedType)field.getGenericType());
                for (Map.Entry<Object, MultiConfiguration> parentEntry : parentFieldMap.entrySet())
                {
                    Node keyNode = Convert.toNode(parentEntry.getKey());
                    if (keyNode instanceof StringNode)
                    {
                        MapNode configNode = MapNode.emptyMap();
                        mapNode.setNode((StringNode)keyNode, configNode);
                        this.createSubContainer(path + codec.PATH_SEPARATOR + ((StringNode)keyNode).getValue())
                            .fillFromFields(parentEntry.getValue(), childFieldMap
                                .get(parentEntry.getKey()), configNode);
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
            throw new InvalidConfigurationException(
                "Error while dumping loaded config into fields!" +
                    "\ncurrent config: " + config.getClass().toString() +
                    "\ncurent field:" + field.getName(), e);
        }
    }

    @Override
    public abstract MultiCodecContainer createSubContainer(String parentPath);
}
