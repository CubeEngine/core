package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.config.node.LongNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyConfigurationConverter implements Converter<CurrencyConfiguration>
{
    @Override
    public Node toNode(CurrencyConfiguration object) throws ConversionException
    {
        MapNode currency = MapNode.emptyMap();
        MapNode subCurrencies = MapNode.emptyMap();
        for (Map.Entry<String, SubCurrencyConfig> entry : object.subcurrencies.entrySet())
        {
            subCurrencies.setNode(new StringNode(entry.getKey()), Convert.toNode(entry.getValue()));
        }
        currency.setNode(new StringNode("sub-currencies"), subCurrencies);
        MapNode firstSub = (MapNode)subCurrencies.getMappedNodes().entrySet().iterator().next().getValue();
        firstSub.removeNode("value", ":");
        MapNode format = MapNode.emptyMap();
        currency.setNode(new StringNode("formatting"), format);
        format.setNode(new StringNode("long"), new StringNode(object.formatLong));
        format.setNode(new StringNode("short"), new StringNode(object.formatShort));
        currency.setNode(new StringNode("default-balance"), new LongNode(object.defaultBalance));
        return currency;
    }

    @Override
    public CurrencyConfiguration fromNode(Node node) throws ConversionException
    {
        try
        {
            MapNode currency = (MapNode)node;
            MapNode subCurrencies = (MapNode)currency.getMappedNodes().get("sub-currencies");
            MapNode format = (MapNode)currency.getMappedNodes().get("formatting");
            LinkedHashMap<String, SubCurrencyConfig> subConfigs = new LinkedHashMap<String, SubCurrencyConfig>();
            for (Map.Entry<String, Node> entry : subCurrencies.getMappedNodes().entrySet())
            {
                subConfigs.put(entry.getKey(), (SubCurrencyConfig)Convert.fromNode(entry.getValue(), SubCurrencyConfig.class));
            }
            Long defaultBalance = Long.parseLong(currency.getMappedNodes().get("default-balance").unwrap());
            CurrencyConfiguration currencyConfig =
                    new CurrencyConfiguration(subConfigs,
                            format.getMappedNodes().get("long").unwrap(),
                            format.getMappedNodes().get("short").unwrap(),
                            defaultBalance);
            return currencyConfig;
        }
        catch (Exception e)
        {
            throw new ConversionException("Could not convert Currency-Configuration!", e);
        }
    }
}
