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
package de.cubeisland.engine.itemrepair.material;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Material;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.NullNode;

public class BaseMaterialContainerConverter implements Converter<BaseMaterialContainer>
{
    private final Type fieldType;
    private Map<Material, Double> map; // Needed for GenericType

    public BaseMaterialContainerConverter()
    {
        try
        {
            fieldType = this.getClass().getDeclaredField("map").getGenericType();
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Node toNode(BaseMaterialContainer object, ConverterManager manager) throws ConversionException
    {
        Map<Material,Double> result = new TreeMap<>(new Comparator<Material>()
        {
            @Override
            public int compare(Material o1, Material o2)
            {
                return o1.name().compareTo(o2.name());
            }
        });
        for (BaseMaterial baseMaterial : object.getBaseMaterials().values())
        {
            result.put(baseMaterial.getMaterial(),baseMaterial.getPrice());
        }
        return new MapNode(result);
    }

    @Override
    public BaseMaterialContainer fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            map = manager.convertFromNode(node, fieldType);
            BaseMaterialContainer container = new BaseMaterialContainer(map);
            map = null;
            return container;
        }
        else if (node instanceof NullNode)
        {
            return null;
        }
        else
        {
            throw ConversionException.of(this, node, "Node is not a MapNode!");
        }
    }
}
