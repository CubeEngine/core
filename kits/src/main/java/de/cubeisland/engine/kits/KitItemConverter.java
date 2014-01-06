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
package de.cubeisland.engine.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.IntNode;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.StringNode;
import de.cubeisland.engine.core.util.matcher.Match;

public class KitItemConverter implements Converter<KitItem>
{
    private static final Pattern pat = Pattern.compile("(?:([0-9]+)\\*)?([a-zA-Z0-9_]+)(?::([0-9]+))?(?: (.+))?(:)?");

    @Override
    public Node toNode(KitItem object, ConverterManager manager) throws ConversionException
    {
        if (object.enchs == null || object.enchs.isEmpty())
        {
            return StringNode.of(object.amount + "*" + object.mat.name() + ":" + object.dura +
                                     (object.customName == null ? "" : " " + object.customName));
        }
        else
        {
            MapNode mapNode = MapNode.emptyMap();
            mapNode.setNode(StringNode.of(object.amount + "*" + object.mat.name() + ":" + object.dura +
                                              (object.customName == null ? "" : " " + object.customName)),
                            manager.convertToNode(object.enchs));
            return mapNode;
        }
    }

    @Override
    public KitItem fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        //suported formats: [amount*]id[:data][ customname]
        // if has enchs as map with map of enchs below
        String itemString;
        if (node instanceof MapNode)
        {
            itemString = ((MapNode)node).getFirstKey();
        }
        else
        {
            itemString = node.asText();
        }
        if (itemString.matches(pat.pattern()))
        {
            Matcher matcher = pat.matcher(itemString);
            matcher.find();
            String materialString = matcher.group(2);
            String duraString = matcher.group(3);
            String amountString = matcher.group(1);
            String name = matcher.group(4);
            int amount;
            short dura;
            try
            {
                Material mat = Match.material().material(materialString);
                if (amountString == null)
                {
                    amount = mat.getMaxStackSize();
                }
                else
                {
                    amount = Integer.parseInt(amountString);
                }
                if (duraString == null)
                {
                    dura = 0;
                }
                else
                {
                    dura = Short.parseShort(duraString);
                }
                Map<Enchantment, Integer> enchs = new HashMap<>();
                if (node instanceof MapNode)
                {
                    MapNode subNode= (MapNode)((MapNode)node).getExactNode(((MapNode)node).getFirstKey());
                    for (Entry<String, Node> enchNode : subNode.getMappedNodes().entrySet())
                    {
                        int lv;
                        Enchantment enchantment = Match.enchant().enchantment(enchNode.getKey());
                        lv = ((IntNode)enchNode.getValue()).getValue();
                        enchs.put(enchantment, lv);
                    }
                }
                return new KitItem(mat, dura, amount, name, enchs);
            }
            catch (Exception ex)
            {
                throw ConversionException.of(this, node, "Could not parse kitItem! " + itemString, ex);
            }
        }
        throw ConversionException.of(this, node, "Could not parse kitItem!");
    }
}
