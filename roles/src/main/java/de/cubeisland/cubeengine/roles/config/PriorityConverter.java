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
package de.cubeisland.cubeengine.roles.config;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class PriorityConverter implements Converter<Priority>
{
    @Override
    public Node toNode(Priority object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.toString());
    }

    @Override
    public Priority fromNode(Node node) throws ConversionException
    {
        Priority prio = Priority.getByName(node.unwrap());
        if (prio == null)
        {
            prio = Priority.getByValue(Integer.valueOf(node.unwrap()));
        }
        return prio;
    }
}
