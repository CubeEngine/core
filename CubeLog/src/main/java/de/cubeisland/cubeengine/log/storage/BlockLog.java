package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

@Entity(name = "blocklog")
public class BlockLog extends AbstractPositionLog //implements Model<Integer>
{
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public BlockData newBlock;
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public BlockData oldBlock; //contains Material and RawMetaData

    @DatabaseConstructor
    public BlockLog(List<Object> args) throws ConversionException
    {
        super(args.subList(0, 7));
        this.newBlock = Convert.fromObject(BlockData.class, args.get(8));
        this.oldBlock = Convert.fromObject(BlockData.class, args.get(9));
    }

    public BlockLog(int causeID, BlockState newBlock, BlockState oldBlock)
    {
        super(causeID, newBlock == null ? oldBlock.getLocation() : newBlock.getLocation());
        if (newBlock == null)
        {
            this.newBlock = new BlockData(Material.AIR, (byte)0);
        }
        else
        {
            this.newBlock = new BlockData(newBlock.getType(), newBlock.getRawData());
        }
        if (oldBlock == null)
        {
            this.oldBlock = new BlockData(Material.AIR, (byte)0);
        }
        else
        {
            this.oldBlock = new BlockData(oldBlock.getType(), oldBlock.getRawData());
        }
    }

    public boolean isBlockBreak()
    {
        return (newBlock.mat == Material.AIR);
    }

    public boolean isBlockPlace()
    {
        return (oldBlock.mat == Material.AIR);
    }

    public boolean isBlockRePlace()
    {
        return ((oldBlock.mat != Material.AIR) && (newBlock.mat != Material.AIR));
    }
}