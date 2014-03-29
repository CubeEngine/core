package de.cubeisland.engine.bigdata;

import com.mongodb.DBRefBase;
import de.cubeisland.engine.bigdata.node.DBRefBaseNode;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.Node;

public class ReferenceConverter implements Converter<Reference>
{
    public ReferenceConverter(Reflector reflector)
    {
        this.reflector = reflector;
    }

    private final Reflector reflector;

    @Override
    public Node toNode(Reference object, ConverterManager manager) throws ConversionException
    {
        return new DBRefBaseNode(object.ref);
    }

    @Override
    public Reference fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof DBRefBaseNode)
        {
            return new Reference(reflector, (DBRefBase)node.getValue());
        }
        throw ConversionException.of(this, node, "Node is not a mapnode!");
    }
}
