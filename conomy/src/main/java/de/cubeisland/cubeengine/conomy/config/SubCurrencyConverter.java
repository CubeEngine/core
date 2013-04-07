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
package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SubCurrencyConverter implements Converter<SubCurrencyConfig>
{
    @Override
    public Node toNode(SubCurrencyConfig object) throws ConversionException
    {
        HashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("longname-plural", object.longNamePlural);
        map.put("shortname", object.shortName);
        map.put("shortname-plural", object.shortNamePlural);
        map.put("value", object.value);
        return Convert.wrapIntoNode(map);
    }

    @Override
    public SubCurrencyConfig fromNode(Node node) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            MapNode mapNode = (MapNode) node;
            try
            {
                String longNamePlural = mapNode.getExactNode("longname-plural").unwrap();
                String shortName = mapNode.getExactNode("shortname").unwrap();
                String shortNamePlural = mapNode.getExactNode("shortname-plural").unwrap();
                Integer value = 100;
                try
                {
                    value = Integer.valueOf(mapNode.getExactNode("value").unwrap());
                }
                catch (NumberFormatException ignored)
                {}
                return new SubCurrencyConfig(longNamePlural,shortName,shortNamePlural, value);
            }
            catch (Exception e)
            {
                throw new ConversionException("Could not parse SubCurrencyConfig!", e);
            }
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
