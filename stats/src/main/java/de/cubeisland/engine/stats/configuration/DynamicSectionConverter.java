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
package de.cubeisland.engine.stats.configuration;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;

public class DynamicSectionConverter implements Converter<DynamicSection>
{
    @Override
    public Node toNode(DynamicSection dynamicSection, ConverterManager converterManager) throws ConversionException
    {
        return dynamicSection.getMapNode();
    }

    @Override
    public DynamicSection fromNode(Node node, ConverterManager converterManager) throws ConversionException
    {
        if (!(node instanceof MapNode))
        {
            throw ConversionException.of(this, node, "Node was not MapNode!");
        }
        return new DynamicSection(converterManager, (MapNode)node);
    }
}
