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

package de.cubeisland.engine.core.util.converter;

import de.cubeisland.engine.core.logging.Level;

import de.cubeisland.engine.configuration.convert.ConversionException;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.node.BooleanNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.StringNode;

import static de.cubeisland.engine.configuration.Configuration.wrapIntoNode;

public class LevelConverter implements Converter<Level>
{
    @Override
    public Node toNode(Level object) throws ConversionException
    {
        return wrapIntoNode(object.toString());
    }

    @Override
    public Level fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            Level lv = Level.toLevel(((StringNode)node).getValue());
            if (lv == null)
            {
                throw new ConversionException("Unknown LogLevel: " + ((StringNode)node).getValue());
            }
            return lv;
        }
        else if (node instanceof BooleanNode && !((BooleanNode)node).getValue())
        { // OFF is interpreted as a boolean false
            return fromNode(new StringNode("OFF"));
        }
        throw new ConversionException("Invalid Node! " + node.getClass());
    }
}
