package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.time.Duration;

public class DurationConverter implements Converter<Duration>
{

    @Override
    public Node toNode(Duration object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.format());
    }

    @Override
    public Duration fromNode(Node node) throws ConversionException
    {
        return new Duration(node.unwrap());
    }
}
