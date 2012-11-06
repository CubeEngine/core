package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.storage.AbstractPositionLog;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.timestamp = (Timestamp)args.get(1);
        this.causeID = Convert.fromObject(Integer.class, args.get(2));
        this.world = Convert.fromObject(World.class, args.get(3));
        this.x = Convert.fromObject(Integer.class, args.get(4));
        this.y = Convert.fromObject(Integer.class, args.get(5));
        this.z = Convert.fromObject(Integer.class, args.get(6));
        this.newBlock = Convert.fromObject(BlockData.class, args.get(7));
        this.oldBlock = Convert.fromObject(BlockData.class, args.get(8));
    }

    public BlockLog(int causeID, BlockState newBlock, BlockState oldBlock)
    {
        // Key gets autoassigned
        this.causeID = causeID;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        if (newBlock == null)
        {
            this.newBlock = new BlockData(Material.AIR, (byte)0);
        }
        else
        {
            Location loc = newBlock.getLocation();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
            this.world = newBlock.getWorld();
            this.newBlock = new BlockData(newBlock.getType(), newBlock.getRawData());
        }
        if (oldBlock == null)
        {
            this.oldBlock = new BlockData(Material.AIR, (byte)0);
        }
        else
        {
            Location loc = oldBlock.getLocation();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
            this.world = oldBlock.getWorld();
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