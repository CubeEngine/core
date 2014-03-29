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
package de.cubeisland.engine.bigdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRefBase;
import de.cubeisland.engine.bigdata.node.DBRefBaseNode;
import de.cubeisland.engine.bigdata.node.ObjectIdNode;
import de.cubeisland.engine.reflect.Reflected;
import de.cubeisland.engine.reflect.codec.Codec;
import de.cubeisland.engine.reflect.exception.CodecIOException;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.ErrorNode;
import de.cubeisland.engine.reflect.node.ListNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.ParentNode;
import org.bson.types.ObjectId;

public class MongoDBCodec extends Codec<RDBObject, RDBObject>
{
    @Override
    public Collection<ErrorNode> loadReflected(Reflected reflected, RDBObject rdbo)
    {
        try
        {
            return dumpIntoSection(reflected.getDefault(), reflected, this.load(rdbo, reflected), reflected);
        }
        catch (ConversionException ex)
        {
            if (reflected.useStrictExceptionPolicy())
            {
                throw new CodecIOException("Could not load reflected", ex);
            }
            reflected.getLogger().warning("Could not load reflected" + ex);
            return Collections.emptyList();
        }
    }

    @Override
    public void saveReflected(Reflected reflected, RDBObject rdbo)
    {
        try
        {
            this.save(convertSection(reflected.getDefault(), reflected, reflected), rdbo, reflected);
        }
        catch (ConversionException ex)
        {
            if (reflected.useStrictExceptionPolicy())
            {
                throw new CodecIOException("Could not save reflected", ex);
            }
            reflected.getLogger().warning("Could not save reflected" + ex);
        }
    }

    @Override
    protected void save(MapNode mapNode, RDBObject rdbo, Reflected reflected) throws ConversionException
    {
        BasicDBObject saveObject = this.convertMapNode(mapNode);
        if (rdbo.getDBObject().get("_id") != null)
        {
            saveObject.append("_id", rdbo.getDBObject().get("_id"));
        }
        rdbo.getCollection().save(saveObject);
    }

    private BasicDBObject convertMapNode(MapNode mapNode)
    {
        if (mapNode.isEmpty())
        {
            return new BasicDBObject();
        }
        BasicDBObject bdbo = null;
        for (Entry<String, Node> entry : mapNode.getMappedNodes().entrySet())
        {
            if (bdbo == null)
            {
                bdbo = new BasicDBObject(entry.getKey(), convertNode(entry.getValue()));
            }
            else
            {
                bdbo.append(entry.getKey(), convertNode(entry.getValue()));
            }
        }
        return bdbo;
    }

    private Object convertNode(Node node)
    {
        if (node instanceof ParentNode)
        {
            if (node instanceof MapNode)
            {
                return convertMapNode((MapNode)node);
            }
            else if (node instanceof ListNode)
            {
                return convertListNode((ListNode)node);
            }
            else
            {
                throw new IllegalArgumentException("ParentNode has to be List or MapNode not a " + node.getClass());
            }
        }
        else
        {
            return node.getValue();
        }
    }

    private List convertListNode(ListNode listNode)
    {
        ArrayList<Object> list = new ArrayList<>();
        if (listNode.isEmpty())
        {
            return list;
        }
        for (Node node : listNode.getValue())
        {
            list.add(convertNode(node));
        }
        return list;
    }


    @Override
    protected MapNode load(RDBObject rdbo, Reflected reflected) throws ConversionException
    {
        return convertDBObjectToNode(rdbo.getDBObject());
    }

    private MapNode convertDBObjectToNode(DBObject dbObject)
    {
        MapNode mapNode = MapNode.emptyMap();
        for (String key : dbObject.keySet())
        {
            Object value = dbObject.get(key);
            Node nodeValue = this.convertDBObjectToNode((DBObject)value);
            mapNode.setExactNode(key, nodeValue);
        }
        return mapNode;
    }

    private Node convertObjectToNode(Object value)
    {
        Node nodeValue;
        if (value instanceof DBObject)
        {
            nodeValue = this.convertDBObjectToNode((DBObject)value);
        }
        else if (value instanceof List)
        {
            nodeValue = this.convertListToNode((List)value);
        }
        else if (value instanceof ObjectId)
        {
            nodeValue = ObjectIdNode.of((ObjectId)value);
        }
        else if (value instanceof DBRefBase)
        {
            nodeValue = DBRefBaseNode.of((DBRefBase)value);
        }
        else
        {
            nodeValue = Node.wrapIntoNode(value);
        }
        return nodeValue;
    }

    private Node convertListToNode(List list)
    {
        ListNode listNode = ListNode.emptyList();
        for (Object value : list)
        {
            listNode.addNode(this.convertObjectToNode(value));
        }
        return listNode;
    }
}
