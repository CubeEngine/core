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
