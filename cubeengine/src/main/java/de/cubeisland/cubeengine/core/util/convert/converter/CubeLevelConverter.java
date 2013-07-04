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
package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.logger.CubeLevel;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class CubeLevelConverter implements Converter<CubeLevel>
{
    @Override
    public Node toNode(CubeLevel object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.toString());
    }

    @Override
    public CubeLevel fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            CubeLevel lv = LogLevel.parse(((StringNode)node).getValue());
            if (lv == null)
            {
                throw new ConversionException("Unknown LogLevel. " + ((StringNode)node).getValue());
            }
            return lv;
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
