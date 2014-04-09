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

import java.util.Date;

import de.cubeisland.engine.bigdata.node.DateNode;
import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.Node;

public class DateConverter implements Converter<Date>
{
    @Override
    public Node toNode(Date object, ConverterManager manager) throws ConversionException
    {
        return new DateNode(object);
    }

    @Override
    public Date fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof DateNode)
        {
            return (Date)node.getValue();
        }
        throw ConversionException.of(this, node, "Not a DateNode");
    }
}
