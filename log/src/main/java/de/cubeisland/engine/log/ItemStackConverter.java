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
package de.cubeisland.engine.log;

import net.minecraft.server.v1_7_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.bukkit.NBTUtils;
import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.IntNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.StringNode;

public class ItemStackConverter implements Converter<ItemStack>
{
    @Override
    public Node toNode(ItemStack itemStack, ConverterManager converterManager) throws ConversionException
    {
        MapNode item = MapNode.emptyMap();
        item.setExactNode("Count", new IntNode(itemStack.getAmount()));
        item.setExactNode("Damage", new IntNode(itemStack.getDurability()));
        item.setExactNode("Item", StringNode.of(itemStack.getType().name()));
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).tag;
        item.setExactNode("tag", tag == null ? MapNode.emptyMap() : NBTUtils.convertNBTToNode(tag));
        return item;
    }


    @Override
    public ItemStack fromNode(Node node, ConverterManager converterManager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Node count = ((MapNode)node).getExactNode("Count");
            Node damage = ((MapNode)node).getExactNode("Damage");
            Node item = ((MapNode)node).getExactNode("Item");
            Node tag = ((MapNode)node).getExactNode("tag");
            if (count instanceof IntNode && damage instanceof IntNode &&
                item instanceof StringNode && (tag instanceof MapNode))
            {
                try
                {
                    ItemStack itemStack = new ItemStack(Material.valueOf(item.asText()));
                    itemStack.setDurability(((IntNode)damage).getValue().shortValue());
                    itemStack.setAmount(((IntNode)count).getValue());
                    net.minecraft.server.v1_7_R3.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                    nms.tag = ((MapNode)tag).isEmpty() ? null : (NBTTagCompound)NBTUtils.convertNodeToNBT(tag);
                    return CraftItemStack.asBukkitCopy(nms);
                }
                catch (IllegalArgumentException e)
                {
                    throw ConversionException.of(this, item, "Unknown Material!");
                }
            }
            else
            {
                throw ConversionException.of(this, node, "Invalid SubNodes!");
            }
        }
        else
        {
            throw ConversionException.of(this, node, "Node is not a MapNode!");
        }
    }
}
