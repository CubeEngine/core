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
package de.cubeisland.engine.core.world;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.StringNode;

public class ConfigWorldConverter implements Converter<ConfigWorld>
{
    private final WorldManager wm;

    public ConfigWorldConverter(WorldManager wm)
    {
        this.wm = wm;
    }

    @Override
    public Node toNode(ConfigWorld configWorld, ConverterManager converterManager) throws ConversionException
    {
        return StringNode.of(configWorld.getName());
    }

    @Override
    public ConfigWorld fromNode(Node node, ConverterManager converterManager) throws ConversionException
    {
        String name = node.asText();
        if (name.contains("{"))
        {
            name = name.substring(0, name.indexOf("{"));
        }
        return new ConfigWorld(this.wm, name);
    }
}
