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
package de.cubeisland.engine.core.util.convert.converter.generic;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;

import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Convert;

public class ArrayConverter
{
    public ListNode toNode(Object[] array) throws ConversionException
    {
        ListNode result = ListNode.emptyList();
        if (array.length == 0)
        {
            return result;
        }
        for (Object value : array)
        {
            result.addNode(Convert.toNode(value));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <V> V[] fromNode(Class<V[]> arrayType, ListNode listNode) throws ConversionException
    {
        Class<V> valueType = (Class<V>)arrayType.getComponentType();
        try
        {
            Collection<V> result = new LinkedList<>();
            for (Node node : listNode.getListedNodes())
            {
                V value = Convert.fromNode(node, valueType);
                result.add(value);
            }
            return result.toArray((V[])Array.newInstance((Class)valueType, result.size()));
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Array-conversion failed: Error while converting the values in the array.");
        }
    }
}
