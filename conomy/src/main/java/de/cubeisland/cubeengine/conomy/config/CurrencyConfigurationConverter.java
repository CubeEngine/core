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
        currency.setNode(new StringNode("minimum-balance"), new LongNode(object.minimumBalance));
        currency.setNode(new StringNode("decimal-separator"), new StringNode(object.decimalSeparator));
        currency.setNode(new StringNode("thousand-separator"), new StringNode(object.thousandSeparator));
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
                subConfigs.put(subCurrencies.getOriginalKey(entry.getKey()), (SubCurrencyConfig)Convert.fromNode(entry.getValue(), SubCurrencyConfig.class));
            }
            Long defaultBalance = 0L;
            try
            {
                defaultBalance = Long.parseLong(currency.getExactNode("default-balance").unwrap());
            }
            catch (NumberFormatException ignored)
            {}
            Long minimumBalance = 0L;
            try
            {
                minimumBalance = Long.parseLong(currency.getExactNode("minimum-balance").unwrap());
            }
            catch (NumberFormatException ignored)
            {}
            CurrencyConfiguration currencyConfig =
                    new CurrencyConfiguration(subConfigs,
                            format.getMappedNodes().get("long").unwrap(),
                            format.getMappedNodes().get("short").unwrap(),
                            defaultBalance, minimumBalance,
                            currency.getExactNode("decimal-separator").unwrap(),
                            currency.getExactNode("thousand-separator").unwrap());
            return currencyConfig;
        }
        catch (Exception e)
        {
            throw new ConversionException("Could not convert Currency-Configuration!", e);
        }
    }
}
