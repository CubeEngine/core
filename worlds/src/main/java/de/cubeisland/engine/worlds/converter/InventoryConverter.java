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
package de.cubeisland.engine.worlds.converter;

import net.minecraft.server.v1_7_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.IntNode;
import de.cubeisland.engine.reflect.node.ListNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.ShortNode;
import de.cubeisland.engine.reflect.node.StringNode;
import de.cubeisland.engine.core.bukkit.NBTUtils;

public class InventoryConverter implements Converter<Inventory>
{
    private final Server server;

    public InventoryConverter(Server server)
    {
        this.server = server;
    }

    @Override
    public Node toNode(Inventory object, ConverterManager manager) throws ConversionException
    {
        MapNode node = MapNode.emptyMap();
        ItemStack[] contents = object.getContents();
        ListNode list = ListNode.emptyList();
        node.setExactNode("Size", new IntNode(object.getSize() + 9));
        node.setExactNode("Contents", list);
        for (int i = 0; i < contents.length; i++)
        {
            ItemStack itemStack = contents[i];
            if (itemStack != null)
            {
                this.addItem(list, itemStack, i);
            }
        }
        if (object instanceof PlayerInventory)
        {
            ItemStack[] armorContents = ((PlayerInventory)object).getArmorContents();
            for (int i = 0; i < armorContents.length; i++)
            {
                ItemStack itemStack = armorContents[i];
                if (itemStack != null && itemStack.getType() != Material.AIR)
                {
                    this.addItem(list, itemStack, i + object.getSize());
                }
            }
        }
        return node;
    }

    private void addItem(ListNode list, ItemStack itemStack, int index)
    {
        MapNode item = MapNode.emptyMap();
        item.setExactNode("Slot", new IntNode(index));
        item.setExactNode("Count", new IntNode(itemStack.getAmount()));
        item.setExactNode("Damage", new ShortNode(itemStack.getDurability()));
        item.setExactNode("Item", StringNode.of(itemStack.getType().name()));
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).tag;
        item.setExactNode("tag", tag == null ? MapNode.emptyMap() : NBTUtils.convertNBTToNode(tag));
        list.addNode(item);
    }

    @Override
    public Inventory fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Node size = ((MapNode)node).getExactNode("Size");
            if (size instanceof IntNode)
            {
                Inventory inventory = server.createInventory(null, ((IntNode)size).getValue());
                Node contents = ((MapNode)node).getExactNode("Contents");
                if (contents instanceof ListNode)
                {
                    for (Node listedNode : ((ListNode)contents).getValue())
                    {
                        if (listedNode instanceof MapNode)
                        {
                            Node slot = ((MapNode)listedNode).getExactNode("Slot");
                            Node count = ((MapNode)listedNode).getExactNode("Count");
                            Node damage = ((MapNode)listedNode).getExactNode("Damage");
                            Node item = ((MapNode)listedNode).getExactNode("Item");
                            Node tag = ((MapNode)listedNode).getExactNode("tag");
                            if (slot instanceof IntNode && count instanceof IntNode && damage instanceof ShortNode &&
                                item instanceof StringNode && (tag instanceof MapNode))
                            {
                                try
                                {
                                    ItemStack itemStack = new ItemStack(Material.valueOf(item.asText()));
                                    itemStack.setDurability(((ShortNode)damage).getValue());
                                    itemStack.setAmount(((IntNode)count).getValue());
                                    net.minecraft.server.v1_7_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                                    nms.tag = ((MapNode)tag).isEmpty() ? null : (NBTTagCompound)NBTUtils.convertNodeToNBT(tag);
                                    inventory.setItem(((IntNode)slot).getValue(), CraftItemStack.asBukkitCopy(nms));
                                }
                                catch (IllegalArgumentException e)
                                {
                                    throw ConversionException.of(this, item, "Unknown Material!");
                                }
                            }
                            else
                            {
                                throw ConversionException.of(this, listedNode, "Invalid SubNodes!");
                            }
                        }
                        else
                        {
                            throw ConversionException.of(this, listedNode, "Node is not a MapNode!");
                        }
                    }
                    return inventory;
                }
                else
                {
                    throw ConversionException.of(this, contents, "Node is not a ListNode!");
                }
            }
            else
            {
                throw ConversionException.of(this, size, "Node is not a IntNode!");
            }
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");

    }
}
