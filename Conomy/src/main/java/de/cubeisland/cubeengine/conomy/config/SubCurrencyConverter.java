package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.util.HashMap;
import java.util.Map;

public class SubCurrencyConverter implements Converter<SubCurrencyConfig>
{
    @Override
    public Node toNode(SubCurrencyConfig object) throws ConversionException
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("value", object.value);
        map.put("shortname", object.shortName);
        return Convert.wrapIntoNode(map);
    }

    @Override
    public SubCurrencyConfig fromNode(Node node) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            try
            {
                Map<String, Node> map = ((MapNode)node).getMappedNodes();
                String shortName = map.get("shortname").unwrap();
                Integer value = 100;
                if (map.get("value") != null)
                {
                    value = Integer.valueOf(map.get("value").unwrap());
                }
                return new SubCurrencyConfig(shortName, value);
            }
            catch (Exception e)
            {
                throw new ConversionException("Could not parse SubCurrencyConfig!", e);
            }
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
