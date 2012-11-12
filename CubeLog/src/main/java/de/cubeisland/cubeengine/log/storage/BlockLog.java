package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

@Entity(name = "blocklog")
public class BlockLog extends AbstractPositionLog
{
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public String newBlock;
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public String oldBlock;
    
    private BlockData newBlockData = null;
    private BlockData oldBlockData = null;

    @DatabaseConstructor
    public BlockLog(List<Object> args) throws ConversionException
    {
        super(args.subList(0, 7));
        this.newBlock = args.get(8).toString();
        this.oldBlock = args.get(9).toString();
    }

    public BlockLog(int causeID, BlockState newBlock, BlockState oldBlock)
    {
        super(causeID, newBlock == null ? oldBlock.getLocation() : newBlock.getLocation());
        try
        {
            if (newBlock == null)
            {
                this.newBlock = (String)Convert.toObject(new BlockData(Material.AIR, (byte)0));
            }
            else
            {
                this.newBlock = (String)Convert.toObject(new BlockData(newBlock.getType(), newBlock.getRawData()));
            }
            if (oldBlock == null)
            {
                this.oldBlock = (String)Convert.toObject(new BlockData(Material.AIR, (byte)0));
            }
            else
            {
                this.oldBlock = (String)Convert.toObject(new BlockData(oldBlock.getType(), oldBlock.getRawData()));
            }
        }
        catch (ConversionException ignored)
        {
            //TODO handle ?
        }
    }

    private void initBlockData()
    {
        try
        {
            if (oldBlockData == null)
            {
                this.oldBlockData = Convert.fromObject(BlockData.class, oldBlock);
            }
            if (newBlockData == null)
            {
                this.newBlockData = Convert.fromObject(BlockData.class, newBlock);
            }
        }
        catch (ConversionException ignored)
        {
        }

    }
    
    public BlockData getNewBlockData()
    {
        this.initBlockData();
        return this.newBlockData;
    }
    
    public BlockData getOldBlockData()
    {
        this.initBlockData();
        return this.oldBlockData;
    }

    public boolean isBlockBreak()
    {
        this.initBlockData();
        return this.newBlockData.mat == Material.AIR;
    }

    public boolean isBlockPlace()
    {
        this.initBlockData();
        return (oldBlockData.mat == Material.AIR);
    }

    public boolean isBlockRePlace()
    {
        this.initBlockData();
        return ((oldBlockData.mat != Material.AIR) && (newBlockData.mat != Material.AIR));
    }
}