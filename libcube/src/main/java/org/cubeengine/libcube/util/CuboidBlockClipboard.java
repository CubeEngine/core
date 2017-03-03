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
package org.cubeengine.libcube.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flowpowered.math.vector.Vector3i;
import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.ConverterManager;
import org.cubeengine.converter.converter.SingleClassConverter;
import org.cubeengine.converter.node.IntNode;
import org.cubeengine.converter.node.ListNode;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.NullNode;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;


/**
 * A simple clipboard for blocks and TileEntity-Data
 */
public class CuboidBlockClipboard
{
    private final BlockData[][][] data;
    private final Vector3i size;
    private Vector3i relative;

    /**
     * Creates a Clipboard containing all BlockData in between pos1 and pos2 in the given world
     *
     * @param world the world
     * @param pos1 origin and position with lowest x,y and z coordinates
     * @param pos2 position with highest x,y and z coordinates
     */
    public CuboidBlockClipboard(Vector3i relativeBlock, World world, Vector3i pos1, Vector3i pos2)
    {
        Vector3i minimum = new Vector3i(pos1.getX() < pos2.getX() ? pos1.getX() : pos2.getX(), pos1.getY() < pos2.getY() ? pos1.getY() : pos2.getY(), pos1.getZ() < pos2.getZ() ? pos1.getZ() : pos2.getZ());
        Vector3i maximum = new Vector3i(pos1.getX() > pos2.getX() ? pos1.getX() : pos2.getX(), pos1.getY() > pos2.getY() ? pos1.getY() : pos2.getY(), pos1.getZ() > pos2.getZ() ? pos1.getZ() : pos2.getZ());
        this.relative = minimum.sub(relativeBlock);
        this.size = maximum.sub(minimum).add(new Vector3i(1,1,1));
        this.data = new BlockData[this.size.getX()][this.size.getY()][this.size.getZ()];
        for (int x = 0; x < this.size.getX(); ++x)
        {
            for (int y = 0; y < this.size.getY(); ++y)
            {
                for (int z = 0; z < this.size.getZ(); ++z)
                {
                    data[x][y][z] = new BlockData(world.getLocation(x + minimum.getX(), y + minimum.getY(), z + minimum.getZ()),minimum);
                }
            }
        }
    }

    public CuboidBlockClipboard(Vector3i size, Vector3i relative)
    {
        this.size = new Vector3i(Math.abs(size.getX()),Math.abs(size.getY()),Math.abs(size.getZ()));
        this.data = new BlockData[this.size.getX()][this.size.getY()][this.size.getZ()];
        this.relative = relative;
    }

    public void setRelativeVector(Vector3i relative)
    {
        this.relative = relative;
    }

    private Map<Byte,BlockType> mappedMaterials;

    public void applyToWorld(World world, Vector3i relative)
    {
        relative = relative.add(this.relative);
        for (int y = 0; y < this.size.getY(); ++y)
        {
            for (int z = 0; z < this.size.getZ(); ++z)
            {
                for (int x = 0; x < this.size.getX(); ++x)
                {
                    /* TODO
                    BlockData blockData = this.data[x][y][z];
                    BlockState state = world.getBlock(relative.getX() + x, relative.getY() + y, relative.getZ() + z);
                    state.setType(blockData.material);
                    state.setRawData(blockData.data);
                    state.update(true,false);
                    if (blockData.nbt != null)
                    {
                        NBTUtils.setTileEntityNBTAt(state.getLocation(),
                                blockData.getRelativeNbtData(relative));
                    }
                    */
                }
            }
        }
    }

    private class BlockData
    {
        private DataContainer dataContainer;
        public BlockType material;

        public BlockData(Location block, Vector3i relative)
        {
            this.material = block.getBlockType();
            this.dataContainer = block.toContainer();
        }
    }

    public static class CuboidBlockClipboardConverter extends SingleClassConverter<CuboidBlockClipboard>
    {
        public CuboidBlockClipboardConverter(Reflector factory) // TODO instance
        {
            ConverterManager cManager = factory.getDefaultConverterManager();
            cManager.registerConverter(this, CuboidBlockClipboard.class);
        }

        @Override
        public Node toNode(CuboidBlockClipboard object, ConverterManager manager) throws ConversionException
        {
            MapNode result = MapNode.emptyMap();
            result.set("width", new IntNode(object.size.getX()));
            result.set("height", new IntNode(object.size.getY()));
            result.set("length", new IntNode(object.size.getZ()));
            if (object.relative != null)
            {
                MapNode relative = MapNode.emptyMap();
                result.set("relative", relative);
                relative.set("x", new IntNode(object.relative.getX()));
                relative.set("y", new IntNode(object.relative.getY()));
                relative.set("z", new IntNode(object.relative.getZ()));
            }
            ListNode tileEntities = ListNode.emptyList();
            result.set("tileentities", tileEntities);
            Map<BlockType,Byte> materials = new HashMap<>();
            object.mappedMaterials = new HashMap<>();
            Byte[] blocks = new Byte[object.size.getX() * object.size.getY() * object.size.getZ()];
            Byte[] bData = new Byte[object.size.getX() * object.size.getY() * object.size.getZ()];
            int i = 0;
            byte lastBdata = 0;
            for (int y = 0; y < object.size.getY(); ++y)
            {
                for (int z = 0; z < object.size.getZ(); ++z)
                {
                    for (int x = 0; x < object.size.getX(); ++x)
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
                        /* TODO
                        bData[i] = curData.data;
                        if (curData.nbt != null)
                        {
                            tileEntities.addNode(convertNBTToNode(curData.nbt));
                        }
                        */
                        i++;
                    }
                }
            }
            result.set("materials", manager.convertToNode(object.mappedMaterials));
            result.set("blocks", manager.convertToNode(blocks));
            result.set("data", manager.convertToNode(bData));
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
                    Map<Byte,BlockType> mappedMaterials = manager.convertFromNode(mappedNodes.get("materials"),CuboidBlockClipboard.class.getDeclaredField("mappedMaterials").getGenericType());
                    Byte[] blocks = manager.convertFromNode(mappedNodes.get("blocks"),Byte[].class);
                    Byte[] data = manager.convertFromNode(mappedNodes.get("data"),Byte[].class);
                    // TODO Map<BlockVector3,NBTTagCompound> tileEntities = new HashMap<>();
                    Node tileE = mappedNodes.get("tileentities");
                    if (tileE != null && tileE instanceof ListNode)
                    {
                        List<Node> listedNodes = ((ListNode)tileE).getValue();
                        for (Node listedNode : listedNodes)
                        {
                            if (listedNode instanceof MapNode)
                            {
                                Map<String, Node> teData = ((MapNode)listedNode).getValue();
                                Vector3i vector = new Vector3i(((IntNode)teData.get("x")).getValue(),
                                                                       ((IntNode)teData.get("y")).getValue(),
                                                                       ((IntNode)teData.get("z")).getValue());
                               // TODO NBTTagCompound tileTag = (NBTTagCompound)NBTUtils.convertNodeToNBT(listedNode);
                                // TODO tileEntities.put(vector,tileTag);

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
                    Vector3i relative = null;
                    if (mappedNodes.containsKey("relative"))
                    {
                        MapNode relativeMap = (MapNode)mappedNodes.get("relative");
                        int x = (Integer)relativeMap.getValue().get("x").getValue();
                        int y = (Integer)relativeMap.getValue().get("y").getValue();
                        int z = (Integer)relativeMap.getValue().get("z").getValue();
                        relative = new Vector3i(x,y,z);
                    }
                    CuboidBlockClipboard result = new CuboidBlockClipboard(new Vector3i(width, height, length),relative);
                    int i = 0;
                    for (int y = 0; y < height; ++y)
                    {
                        for (int z = 0; z < length; ++z)
                        {
                            for (int x = 0; x < width; ++x)
                            {
                                BlockType mat = mappedMaterials.get(blocks[i]);
                                // TODO result.data[x][y][z] = result.new BlockData(mat,data[i]);
                                i++;
                            }
                        }
                    }
                    // TODO  for (Entry<BlockVector3, NBTTagCompound> entry : tileEntities.entrySet())
                    {
                        // TODO       result.data[entry.getKey().getX()][entry.getKey().getY()][entry.getKey().getZ()].nbt = entry.getValue();
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
