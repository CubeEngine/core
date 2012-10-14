package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.LogManager.BlockChangeCause;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

@Entity(name = "blocklog")
public class BlockLog implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp timestamp;
    @Attribute(type = AttrType.INT)
    public int userID;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public World world;
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public BlockData newBlock;
    @Attribute(type = AttrType.VARCHAR, length = 10)
    public BlockData oldBlock; //contains Material and RawMetaData

    @DatabaseConstructor
    public BlockLog(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.timestamp = (Timestamp)args.get(1);
        this.userID = Convert.fromObject(Integer.class, args.get(2));
        this.world = Convert.fromObject(World.class, args.get(3));
        this.x = Convert.fromObject(Integer.class, args.get(4));
        this.y = Convert.fromObject(Integer.class, args.get(5));
        this.z = Convert.fromObject(Integer.class, args.get(6));
        this.newBlock = Convert.fromObject(BlockData.class, args.get(7));
        this.oldBlock = Convert.fromObject(BlockData.class, args.get(8));

    }

    public BlockLog(BlockChangeCause cause, long timestamp, BlockState newBlock, BlockState oldBlock)
    {
        this(timestamp, null, newBlock, oldBlock);
        switch (cause)
        {// -1 is for unknown player (should never get used)
            case ENDERMAN:
                this.userID = -2;
                break;
            case EXPLOSION:
                this.userID = -3;
                break;
            case FIRE:
                this.userID = -4;
                break;
            case FADE:
            case FORM:
                this.userID = -5;
                break;
            case PLAYER:
                throw new IllegalStateException("When the BlockBreakCause was a player use the other constructor!");
        }
    }

    public BlockLog(long timestamp, Player user, BlockState newBlock, BlockState oldBlock)
    {
        // Key gets autoassigned
        this.timestamp = new Timestamp(timestamp);
        if (user == null)
        {
            this.userID = -1;
        }
        else
        {
            this.userID = CubeEngine.getUserManager().getUser(user).getKey();
        }


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

    public Integer getKey()
    {
        return key;
    }

    public void setKey(Integer key)
    {
        this.key = key;
    }

    public Location getLocation()
    {
        return new Location(world, x, y, z);
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

    public boolean isCausedByPlayer()
    {
        return (userID > 0);
    }

    public User getUser()
    {
        if (this.isCausedByPlayer())
        {
            return CubeEngine.getUserManager().getUser(userID);
        }
        return null;
    }

    public long getTimeStamp()
    {
        return this.timestamp.getTime();
    }
}