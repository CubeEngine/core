package de.cubeisland.cubeengine.core.util.convert.converter;

import java.util.Locale;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class LocaleConverter implements Converter<Locale>
{
    @Override
    public Node toNode(Locale locale) throws ConversionException
    {
        return new StringNode(I18n.localeToString(locale));
    }

    @Override
    public Locale fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return I18n.stringToLocale(((StringNode)node).getValue());
        }
        throw new ConversionException("Locales can only be loaded from a string node!");
    }
}
