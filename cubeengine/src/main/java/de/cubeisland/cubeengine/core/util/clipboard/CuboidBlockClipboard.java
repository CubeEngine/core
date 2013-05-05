package de.cubeisland.cubeengine.core.util.clipboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagInt;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import de.cubeisland.cubeengine.core.bukkit.NBTUtils;
import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.NullNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.converter.CuboidBlockClipboardConverter;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;

/**
 * A simple clipboard for blocks and TileEntity-Data
 */
public class CuboidBlockClipboard
{
    static
    {
        Convert.registerConverter(CuboidBlockClipboard.class,new CuboidBlockClipboardConverter()); // register converter for configs
    }

    private final BlockData[][][] data;
    private final BlockVector3 size;

    /**
     * Creates a Clipboard containing all BlockData in between pos1 and pos2 in the given world
     *
     * @param world the world
     * @param pos1 origin and position with lowest x,y and z coordinates
     * @param pos2 position with highest x,y and z coordinates
     */
    public CuboidBlockClipboard(World world, BlockVector3 pos1, BlockVector3 pos2)
    {
        this.size = pos1.subtract(pos2).add(new BlockVector3(1,1,1));
        this.data = new BlockData[this.size.x][this.size.y][this.size.z];

        for (int x = 0; x < size.x; ++x)
        {
            for (int y = 0; y < size.y; ++y)
            {
                for (int z = 0; z < size.z; ++z)
                {
                    data[x][y][z] = new BlockData(world.getBlockAt(x+pos1.x,y+pos1.y,z+ pos1.z),pos1);
                }
            }
        }
    }

    public CuboidBlockClipboard(BlockVector3 size)
    {
        this.size = size;
        this.data = new BlockData[this.size.x][this.size.y][this.size.z];
    }

    private Map<Byte,Material> mappedMaterials;

    public Node toNode() throws ConversionException
    {
        MapNode result = MapNode.emptyMap();
        result.setExactNode("width",new IntNode(this.size.x));
        result.setExactNode("height",new IntNode(this.size.y));
        result.setExactNode("length",new IntNode(this.size.z));
        ListNode tileEntities = ListNode.emptyList();
        result.setExactNode("tileentities",tileEntities);
        Map<Material,Byte> materials = new HashMap<Material, Byte>();
        this.mappedMaterials = new HashMap<Byte, Material>();
        Byte[] blocks = new Byte[this.size.x * this.size.y * this.size.z];
        Byte[] bData = new Byte[this.size.x * this.size.y * this.size.z];
        int i = 0;
        byte lastBdata = 0;
        for (int y = 0; y < size.y; ++y)
        {
            for (int z = 0; z < size.z; ++z)
            {
                for (int x = 0; x < size.x; ++x)
                {
                    BlockData curData = data[x][y][z];
                    Byte block = materials.get(curData.material);
                    if (block == null)
                    {
                        block = lastBdata++;
                        materials.put(curData.material,block);
                        this.mappedMaterials.put(block,curData.material);
                    }
                    blocks[i] = block;
                    bData[i] = curData.data;
                    if (curData.nbt != null)
                    {
                        tileEntities.addNode(NBTUtils.convertNBTToNode(curData.nbt));
                    }
                    i++;
                }
            }
        }
        result.setExactNode("materials",Convert.toNode(this.mappedMaterials));
        result.setExactNode("blocks",Convert.toNode(blocks));
        result.setExactNode("data",Convert.toNode(bData));
        return result;
    }

    public static CuboidBlockClipboard fromNode(Node node) throws ConversionException
    {
        try
        {
            if (node instanceof MapNode)
            {
                LinkedHashMap<String,Node> mappedNodes = ((MapNode)node).getMappedNodes();
                int width = Convert.fromNode(mappedNodes.get("width"), Integer.class);
                int height = Convert.fromNode(mappedNodes.get("height"), Integer.class);
                int length = Convert.fromNode(mappedNodes.get("length"), Integer.class);
                Map<Byte,Material> mappedMaterials = Convert.fromNode(mappedNodes.get("materials"),CuboidBlockClipboard.class.getDeclaredField("mappedMaterials").getGenericType());
                Byte[] blocks = Convert.fromNode(mappedNodes.get("blocks"),Byte[].class);
                Byte[] data = Convert.fromNode(mappedNodes.get("data"),Byte[].class);
                Map<BlockVector3,NBTTagCompound> tileEntities = new HashMap<BlockVector3, NBTTagCompound>();
                Node tileE = mappedNodes.get("tileentities");
                if (tileE != null && tileE instanceof ListNode)
                {
                    ArrayList<Node> listedNodes = ((ListNode)tileE).getListedNodes();
                    for (Node listedNode : listedNodes)
                    {
                        if (listedNode instanceof MapNode)
                        {
                            LinkedHashMap<String, Node> teData = ((MapNode)listedNode).getMappedNodes();
                            BlockVector3 vector = new BlockVector3(((IntNode)teData.get("x")).getValue(),
                                                                    ((IntNode)teData.get("y")).getValue(),
                                                                    ((IntNode)teData.get("z")).getValue());
                            NBTTagCompound tileTag = (NBTTagCompound)NBTUtils.convertNodeToNBT("", listedNode);
                            tileEntities.put(vector,tileTag);

                        }
                        else if (listedNode instanceof NullNode)
                        {
                            continue; // ignore NullNode
                        }
                        else
                        {
                            throw new ConversionException("TileEntityData was not in a MapNode!");
                        }
                    }
                }
                CuboidBlockClipboard result = new CuboidBlockClipboard(new BlockVector3(width, height, length));
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
            throw new ConversionException("Cannot create CuboidBlockClipboard",e);
        }
        throw new ConversionException("Cannot create CuboidBlockClipboard");
    }

    public void applyToWorld(World world, BlockVector3 relative)
    {
        for (int y = 0; y < this.size.y; ++y)
        {
            for (int z = 0; z < this.size.z; ++z)
            {
                for (int x = 0; x < this.size.x; ++x)
                {
                    BlockData blockData = this.data[x][y][z];
                    BlockState state = world.getBlockAt(relative.x + x, relative.y + y, relative.z + z).getState();
                    state.setType(blockData.material);
                    state.setRawData(blockData.data);
                    state.update(true,false);
                    if (blockData.nbt != null)
                    {
                        NBTUtils.setTileEntityNBTAt(state.getLocation(),blockData.getRelativeNbtData(relative));
                        ;
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
            nbt.set("x",new NBTTagInt("x",block.getX() - relative.x));
            nbt.set("y",new NBTTagInt("y",block.getY() - relative.y));
            nbt.set("z",new NBTTagInt("z",block.getZ() - relative.z));
        }

        public NBTTagCompound getRelativeNbtData(BlockVector3 relative)
        {
            NBTTagCompound clone = (NBTTagCompound)this.nbt.clone();
            clone.set("x",new NBTTagInt("x",clone.getInt("x") + relative.x));
            clone.set("y",new NBTTagInt("y",clone.getInt("y") + relative.y));
            clone.set("z",new NBTTagInt("z",clone.getInt("z") + relative.z));
            return clone;
        }

        public BlockData(Material mat, byte b)
        {
            this.material = mat;
            this.data = b;
        }
    }
}
