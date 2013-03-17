package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.util.Locale;

public class LocaleConverter implements Converter<Locale>
{
    @Override
    public Node toNode(Locale locale) throws ConversionException
    {
        return new StringNode(locale.toString());
    }

    @Override
    public Locale fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Locale.forLanguageTag(((StringNode)node).getValue());
        }
        throw new ConversionException("Locales can only be loaded from a string node!");
    }
}
