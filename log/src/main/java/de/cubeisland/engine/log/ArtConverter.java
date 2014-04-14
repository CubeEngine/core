package de.cubeisland.engine.log;

import org.bukkit.Art;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.StringNode;

public class ArtConverter implements Converter<Art>
{
    @Override
    public Node toNode(Art object, ConverterManager manager) throws ConversionException
    {
        return StringNode.of(object.name());
    }

    @Override
    public Art fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        return Art.valueOf(node.asText());
    }
}
