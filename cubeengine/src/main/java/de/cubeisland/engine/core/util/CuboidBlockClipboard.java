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
package de.cubeisland.engine.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagInt;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.NBTUtils;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.reflect.codec.ConverterManager;
import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.IntNode;
import de.cubeisland.engine.reflect.node.ListNode;
import de.cubeisland.engine.reflect.node.MapNode;
import de.cubeisland.engine.reflect.node.Node;
import de.cubeisland.engine.reflect.node.NullNode;

import static de.cubeisland.engine.core.bukkit.NBTUtils.convertNBTToNode;

/**
 * A simple clipboard for blocks and TileEntity-Data
 */
public class CuboidBlockClipboard
{
    static
    {
        new CuboidBlockClipboardConverter(CubeEngine.getCore().getConfigFactory());
    }

    private final BlockData[][][] data;
    private final BlockVector3 size;
    private BlockVector3 relative;

    /**
     * Creates a Clipboard containing all BlockData in between pos1 and pos2 in the given world
     *
     * @param world the world
     * @param pos1 origin and position with lowest x,y and z coordinates
     * @param pos2 position with highest x,y and z coordinates
     */
    public CuboidBlockClipboard(BlockVector3 relativeBlock, World world, BlockVector3 pos1, BlockVector3 pos2)
    {
        BlockVector3 minimum = new BlockVector3(pos1.x < pos2.x ? pos1.x : pos2.x, pos1.y < pos2.y ? pos1.y : pos2.y, pos1.z < pos2.z ? pos1.z : pos2.z);
        BlockVector3 maximum = new BlockVector3(pos1.x > pos2.x ? pos1.x : pos2.x, pos1.y > pos2.y ? pos1.y : pos2.y, pos1.z > pos2.z ? pos1.z : pos2.z);
        this.relative = minimum.subtract(relativeBlock);
        this.size = maximum.subtract(minimum).add(new BlockVector3(1,1,1));
        this.data = new BlockData[this.size.x][this.size.y][this.size.z];
        for (int x = 0; x < this.size.x; ++x)
        {
            for (int y = 0; y < this.size.y; ++y)
            {
                for (int z = 0; z < this.size.z; ++z)
                {
                    data[x][y][z] = new BlockData(world.getBlockAt(x+minimum.x,y+minimum.y,z+ minimum.z),minimum);
                }
            }
        }
    }

    public CuboidBlockClipboard(BlockVector3 size, BlockVector3 relative)
    {
        this.size = new BlockVector3(Math.abs(size.x),Math.abs(size.y),Math.abs(size.z));
        this.data = new BlockData[this.size.x][this.size.y][this.size.z];
        this.relative = relative;
    }

    public void setRelativeVector(BlockVector3 relative)
    {
        this.relative = relative;
    }

    private Map<Byte,Material> mappedMaterials;

    public void applyToWorld(World world, BlockVector3 relative)
    {
        relative = relative.add(this.relative);
        for (int y = 0; y < this.size.y; ++y)
        {
            for (int z = 0; z < this.size.z; ++z)
            {
                for (int x = 0; x < this.size.x; ++x)
                {
                    BlockData blockData = this.data[x][y][z];
                    BlockState state = world.getBlockAt(relative.x + x,
                                                        relative.y + y,
                                                        relative.z + z).getState();
                    state.setType(blockData.material);
                    state.setRawData(blockData.data);
                    state.update(true,false);
                    if (blockData.nbt != null)
                    {
                        NBTUtils.setTileEntityNBTAt(state.getLocation(),
                                blockData.getRelativeNbtData(relative));
                    }
                }
            }
        }
    }

    private class BlockData
    {
        public Material material;
        private byte data;
        private NBTTagCompound nbt; // x,y,z are relative to origin

        private BlockData(Block block, BlockVector3 relative)
        {
            this.material = block.getType();
            this.data = block.getData();
            this.nbt = NBTUtils.getTileEntityNBTAt(block.getLocation());
            if (nbt != null)
            {
                nbt.set("x",new NBTTagInt(block.getX() - relative.x));
                nbt.set("y",new NBTTagInt(block.getY() - relative.y));
                nbt.set("z",new NBTTagInt(block.getZ() - relative.z));
            }
        }

        public NBTTagCompound getRelativeNbtData(BlockVector3 relative)
        {
            NBTTagCompound clone = (NBTTagCompound)this.nbt.clone();
            clone.set("x",new NBTTagInt(clone.getInt("x") + relative.x));
            clone.set("y",new NBTTagInt(clone.getInt("y") + relative.y));
            clone.set("z",new NBTTagInt(clone.getInt("z") + relative.z));
            return clone;
        }

        public BlockData(Material mat, byte b)
        {
            this.material = mat;
            this.data = b;
        }
    }

    public static class CuboidBlockClipboardConverter implements Converter<CuboidBlockClipboard>
    {
        public CuboidBlockClipboardConverter(Reflector factory)
        {
            ConverterManager cManager = factory.getDefaultConverterManager();
            cManager.registerConverter(CuboidBlockClipboard.class, this);
        }

        @Override
        public Node toNode(CuboidBlockClipboard object, ConverterManager manager) throws ConversionException
        {
            MapNode result = MapNode.emptyMap();
            result.setExactNode("width",new IntNode(object.size.x));
            result.setExactNode("height",new IntNode(object.size.y));
            result.setExactNode("length",new IntNode(object.size.z));
            if (object.relative != null)
            {
                MapNode relative = MapNode.emptyMap();
                result.setExactNode("relative",relative);
                relative.setExactNode("x",new IntNode(object.relative.x));
                relative.setExactNode("y",new IntNode(object.relative.y));
                relative.setExactNode("z",new IntNode(object.relative.z));
            }
            ListNode tileEntities = ListNode.emptyList();
            result.setExactNode("tileentities",tileEntities);
            Map<Material,Byte> materials = new HashMap<>();
            object.mappedMaterials = new HashMap<>();
            Byte[] blocks = new Byte[object.size.x * object.size.y * object.size.z];
            Byte[] bData = new Byte[object.size.x * object.size.y * object.size.z];
            int i = 0;
            byte lastBdata = 0;
            for (int y = 0; y < object.size.y; ++y)
            {
                for (int z = 0; z < object.size.z; ++z)
                {
                    for (int x = 0; x < object.size.x; ++x)
                    {
                        BlockData curData = object.data[x][y][z];
                        Byte block = materials.get(curData.material);
                        if (block == null)
                        {
                            block = lastBdata++;
                            materials.put(curData.material,block);
                            object.mappedMaterials.put(block,curData.material);
                        }
                        blocks[i] = block;
                        bData[i] = curData.data;
                        if (curData.nbt != null)
                        {
                            tileEntities.addNode(convertNBTToNode(curData.nbt));
                        }
                        i++;
                    }
                }
            }
            result.setExactNode("materials", manager.convertToNode(object.mappedMaterials));
            result.setExactNode("blocks", manager.convertToNode(blocks));
            result.setExactNode("data", manager.convertToNode(bData));
            return result;
        }

        @Override
        public CuboidBlockClipboard fromNode(Node node, ConverterManager manager) throws ConversionException
        {
            try
            {
                if (node instanceof MapNode)
                {
                    Map<String,Node> mappedNodes = ((MapNode)node).getValue();
                    int width = manager.convertFromNode(mappedNodes.get("width"), Integer.class);
                    int height = manager.convertFromNode(mappedNodes.get("height"), Integer.class);
                    int length = manager.convertFromNode(mappedNodes.get("length"), Integer.class);
                    Map<Byte,Material> mappedMaterials = manager.convertFromNode(mappedNodes.get("materials"),CuboidBlockClipboard.class.getDeclaredField("mappedMaterials").getGenericType());
                    Byte[] blocks = manager.convertFromNode(mappedNodes.get("blocks"),Byte[].class);
                    Byte[] data = manager.convertFromNode(mappedNodes.get("data"),Byte[].class);
                    Map<BlockVector3,NBTTagCompound> tileEntities = new HashMap<>();
                    Node tileE = mappedNodes.get("tileentities");
                    if (tileE != null && tileE instanceof ListNode)
                    {
                        List<Node> listedNodes = ((ListNode)tileE).getValue();
                        for (Node listedNode : listedNodes)
                        {
                            if (listedNode instanceof MapNode)
                            {
                                Map<String, Node> teData = ((MapNode)listedNode).getValue();
                                BlockVector3 vector = new BlockVector3(((IntNode)teData.get("x")).getValue(),
                                                                       ((IntNode)teData.get("y")).getValue(),
                                                                       ((IntNode)teData.get("z")).getValue());
                                NBTTagCompound tileTag = (NBTTagCompound)NBTUtils.convertNodeToNBT(listedNode);
                                tileEntities.put(vector,tileTag);

                            }
                            else if (listedNode instanceof NullNode)
                            {
                                continue; // ignore NullNode
                            }
                            else
                            {
                                throw ConversionException.of(this, null, "TileEntityData was not in a MapNode!");
                            }
                        }
                    }
                    BlockVector3 relative = null;
                    if (mappedNodes.containsKey("relative"))
                    {
                        MapNode relativeMap = (MapNode)mappedNodes.get("relative");
                        int x = (Integer)relativeMap.getValue().get("x").getValue();
                        int y = (Integer)relativeMap.getValue().get("y").getValue();
                        int z = (Integer)relativeMap.getValue().get("z").getValue();
                        relative = new BlockVector3(x,y,z);
                    }
                    CuboidBlockClipboard result = new CuboidBlockClipboard(new BlockVector3(width, height, length),relative);
                    int i = 0;
                    for (int y = 0; y < height; ++y)
                    {
                        for (int z = 0; z < length; ++z)
                        {
                            for (int x = 0; x < width; ++x)
                            {
                                Material mat = mappedMaterials.get(blocks[i]);
                                result.data[x][y][z] = result.new BlockData(mat,data[i]);
                                i++;
                            }
                        }
                    }
                    for (Entry<BlockVector3, NBTTagCompound> entry : tileEntities.entrySet())
                    {
                        result.data[entry.getKey().x][entry.getKey().y][entry.getKey().z].nbt = entry.getValue();
                    }
                    return result;
                }
            }
            catch (Exception e)
            {
                throw ConversionException.of(this, null, "Cannot create CuboidBlockClipboard",e);
            }
            throw ConversionException.of(this, null, "Cannot create CuboidBlockClipboard");
        }
    }
}
