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
package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class CollectionConverter
{
    /**
     * Returns the converted collection
     *
     * @param collection the collection to convert
     * @return the converted collection
     * @throws ConversionException
     */
    public ListNode toNode(Collection collection) throws ConversionException
    {
        ListNode result = ListNode.emptyList();
        if (collection.isEmpty())
        {
            return result;
        }
        for (Object value : collection)
        {
            result.addNode(Convert.toNode(value));
        }
        return result;
    }

    /**
     * Deserializes an object back to a collection
     *
     * @param <V>            the ValueType
     * @param <S>            the Type of collection
     * @param pType          the Type of the collection
     * @param listNode       the Node to convert
     * @return the converted collection
     * @throws ConversionException
     */
    @SuppressWarnings("unchecked")
    public <V, S extends Collection<V>> S fromNode(ParameterizedType pType, ListNode listNode) throws ConversionException
    {
        try
        {
            if (pType.getRawType() instanceof Class)
            {
                S result = (S)getCollectionFor(pType);
                Type subType = pType.getActualTypeArguments()[0];
                for (Node node : listNode.getListedNodes())
                {
                    V value = Convert.fromNode(node, subType);
                    result.add(value);
                }
                return result;
            }
            throw new IllegalArgumentException("Unknown Collection-Type: " + pType);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Collection-conversion failed: Error while converting the values in the collection.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <S extends Collection> S getCollectionFor(ParameterizedType ptype)
    {
        try
        {
            Class<S> collectionType = (Class)ptype.getRawType();
            S result;
            if (collectionType.isInterface() || Modifier.isAbstract(collectionType.getModifiers()))
            {
                if (Set.class.isAssignableFrom(collectionType))
                {
                    if (SortedSet.class.isAssignableFrom(collectionType))
                    {
                        result = (S)new TreeSet();
                    }
                    else
                    {
                        result = (S)new HashSet();
                    }
                }
                else if (List.class.isAssignableFrom(collectionType))
                {
                    result = (S)new LinkedList();
                }
                else
                {
                    result = (S)new LinkedList(); // other collection
                }
            }
            else
            {
                result = collectionType.newInstance();
            }
            return result;
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not access the default constructor of: " + ptype.getRawType(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not create an instance of: " + ptype.getRawType(), ex);
        }
    }
}
