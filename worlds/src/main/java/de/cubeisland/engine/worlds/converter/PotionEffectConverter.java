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
package de.cubeisland.engine.worlds.converter;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.ByteNode;
import de.cubeisland.engine.configuration.node.IntNode;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.NullNode;
import de.cubeisland.engine.configuration.node.StringNode;

public class PotionEffectConverter implements Converter<PotionEffect>
{
    @Override
    public Node toNode(PotionEffect object, ConverterManager manager) throws ConversionException
    {
        MapNode mapNode = MapNode.emptyMap();
        mapNode.setExactNode("amplifier", Node.wrapIntoNode(object.getAmplifier()));
        mapNode.setExactNode("duration", Node.wrapIntoNode(object.getDuration()));
        mapNode.setExactNode("type", Node.wrapIntoNode(object.getType().getName()));
        mapNode.setExactNode("ambient", Node.wrapIntoNode(object.isAmbient()));
        return mapNode;
    }

    @Override
    public PotionEffect fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof NullNode)
        {
            return null;
        }
        if (node instanceof MapNode)
        {
            Node amplifier = ((MapNode)node).getExactNode("amplifier");
            Node duration = ((MapNode)node).getExactNode("duration");
            Node type = ((MapNode)node).getExactNode("type");
            Node ambient = ((MapNode)node).getExactNode("ambient");
            if (amplifier instanceof IntNode && duration instanceof IntNode && type instanceof StringNode && ambient instanceof ByteNode)
            {
                PotionEffectType byName = PotionEffectType.getByName(type.asText());
                if (byName != null)
                {
                    return new PotionEffect(byName, ((IntNode)duration).getValue(), ((IntNode)amplifier).getValue(), ambient.getValue() == 1);
                }
                else
                {
                    throw ConversionException.of(this, node, "Unknown PotionEffectType " + type.asText());
                }
            }
        }
        throw ConversionException.of(this, node, "Invalid NodeTypes!");
    }
}
