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
package de.cubeisland.cubeengine.core.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import net.minecraft.server.v1_5_R2.*;
import net.minecraft.server.v1_5_R2.NBTTagByte;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagEnd;
import net.minecraft.server.v1_5_R2.TileEntity;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.config.node.ByteNode;
import de.cubeisland.cubeengine.core.config.node.DoubleNode;
import de.cubeisland.cubeengine.core.config.node.FloatNode;
import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.LongNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.NullNode;
import de.cubeisland.cubeengine.core.config.node.ShortNode;
import de.cubeisland.cubeengine.core.config.node.StringNode;

public class NBTUtils
{
    public static NBTTagCompound getTileEntityNBTAt(Location location)
    {
        NBTTagCompound result = new NBTTagCompound();
        TileEntity tileEntity = ((CraftWorld)location.getWorld()).getHandle()
             .getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (tileEntity == null) return null;
        tileEntity.b(result);
        return result;
    }

    public static void setTileEntityNBTAt(Location location, NBTTagCompound nbtData)
    {
        TileEntity tileEntity =  ((CraftWorld)location.getWorld()).getHandle()
                      .getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        tileEntity.a(nbtData);
    }

    @SuppressWarnings("unchecked")
    public static Node convertNBTToNode(NBTBase nbtBase)
    {
        if (nbtBase == null) return null;
        if (nbtBase instanceof NBTTagEnd)
        {
            return NullNode.emptyNode();
        }
        if (nbtBase instanceof NBTTagByte)
        {
            return new ByteNode(((NBTTagByte)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagShort)
        {
            return new ShortNode(((NBTTagShort)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagInt)
        {
            return  new IntNode(((NBTTagInt)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagLong)
        {
            return new LongNode(((NBTTagLong)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagFloat)
        {
            return new FloatNode(((NBTTagFloat)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagDouble)
        {
            return new DoubleNode(((NBTTagDouble)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagByteArray)
        {
            ListNode list = ListNode.emptyList();
            for (byte b : ((NBTTagByteArray)nbtBase).data)
            {
                list.addNode(new ByteNode(b));
            }
            return list;
        }
        if (nbtBase instanceof NBTTagString)
        {
            return StringNode.of(((NBTTagString)nbtBase).data);
        }
        if (nbtBase instanceof NBTTagList)
        {
            ListNode list = ListNode.emptyList();
            for (int i = 0; i < ((NBTTagList)nbtBase).size(); i++)
            {
                list.addNode(convertNBTToNode(((NBTTagList)nbtBase).get(i)));
            }
            return list;
        }
        if (nbtBase instanceof NBTTagCompound)
        {
            MapNode map = MapNode.emptyMap();
            for (NBTBase base : (Collection<NBTBase>)((NBTTagCompound)nbtBase).c())
            {
                map.setExactNode(base.getName(),convertNBTToNode(base));
            }
            return map;
        }
        if (nbtBase instanceof NBTTagIntArray)
        {
            ListNode list = ListNode.emptyList();
            for (int i : ((NBTTagIntArray)nbtBase).data)
            {
                list.addNode(new IntNode(i));
            }
            return list;
        }
        throw new IllegalStateException("Unknown NbtTag-Type! "+ nbtBase.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public static NBTBase convertNodeToNBT(String key, Node node)
    {
        if (node instanceof NullNode)
        {
            return new NBTTagEnd();
        }
        if (node instanceof ByteNode)
        {
            return new NBTTagByte(key, (Byte)node.getValue());
        }
        if (node instanceof ShortNode)
        {
            return new NBTTagShort(key, (Short)node.getValue());
        }
        if (node instanceof IntNode)
        {
            return new NBTTagInt(key, (Integer)node.getValue());
        }
        if (node instanceof LongNode)
        {
            return new NBTTagLong(key, (Long)node.getValue());
        }
        if (node instanceof FloatNode)
        {
            return new NBTTagFloat(key, (Float)node.getValue());
        }
        if (node instanceof DoubleNode)
        {
            return new NBTTagDouble(key, (Double)node.getValue());
        }
        if (node instanceof ListNode)
        {
            boolean onlyByte = true;
            boolean onlyInt = true;
            ArrayList<Node> listedNodes = ((ListNode)node).getListedNodes();
            for (Node listedNode : listedNodes)
            {
                if (!(listedNode instanceof IntNode))
                {
                    onlyInt = false;
                }
                if (!(listedNode instanceof ByteNode))
                {
                    onlyByte = false;
                }
            }
            if (onlyByte)
            {
                byte[] byteArray = new byte[listedNodes.size()];
                for (int i = 0 ; i < byteArray.length ; i++)
                {
                    byteArray[i] = (Byte)listedNodes.get(i).getValue();
                }
                return new NBTTagByteArray(key, byteArray);
            }
            if (onlyInt)
            {
                int[] intarray = new int[listedNodes.size()];
                for (int i = 0 ; i < intarray.length ; i++)
                {
                    intarray[i] = (Integer)listedNodes.get(i).getValue();
                }
                return new NBTTagIntArray(key, intarray);
            }
            NBTTagList list = new NBTTagList(key);
            Integer i = 0;
            for (Node listedNode : listedNodes)
            {
                list.add(convertNodeToNBT(i.toString(),listedNode));
                i++;
            }
            return list;

        }
        if (node instanceof StringNode)
        {
            return new NBTTagString(key, (String)node.getValue());
        }
        if (node instanceof MapNode)
        {
            NBTTagCompound compound = new NBTTagCompound(key);
            for (Entry<String, Node> entry : ((MapNode)node).getMappedNodes().entrySet())
            {
                compound.set(((MapNode)node).getOriginalKey(entry.getKey()),convertNodeToNBT(entry.getKey(),entry.getValue()));
            }
            return compound;
        }
        throw new IllegalStateException("Cannot convert nodes to NbtTags! "+ node.getClass().getName());
    }

}
