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
package de.cubeisland.engine.core.util.formatter;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.StringNode;

public class MessageTypeConverter implements Converter<MessageType>
{
    @Override
    public Node toNode(MessageType object, ConverterManager manager) throws ConversionException
    {
        return StringNode.of(object.getName());
    }

    @Override
    public MessageType fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        MessageType messageType = MessageType.valueOf(node.asText());
        if (messageType == null)
        {
            throw ConversionException.of(this, node, "Could not find MessageType");
        }
        return messageType;
    }
}
