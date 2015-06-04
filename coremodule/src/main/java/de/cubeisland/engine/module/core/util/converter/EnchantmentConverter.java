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
package de.cubeisland.engine.module.core.util.converter;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.util.matcher.EnchantMatcher;
import org.spongepowered.api.item.Enchantment;

public class EnchantmentConverter extends SimpleConverter<Enchantment>
{
    private EnchantMatcher enchantMatcher;

    public EnchantmentConverter(EnchantMatcher enchantMatcher)
    {
        this.enchantMatcher = enchantMatcher;
    }

    @Override
    public Node toNode(Enchantment object) throws ConversionException
    {
        return StringNode.of(object.getName());
    }

    @Override
    public Enchantment fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return enchantMatcher.enchantment(((StringNode)node).getValue());
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}