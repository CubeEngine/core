package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class AbstractPositionLog implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp timestamp;
    @Attribute(type = AttrType.INT)
    public int causeID;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public String worldUUID;
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;

    public AbstractPositionLog(int causeID, Location loc)
    {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.causeID = causeID;
        this.worldName = loc.getWorld().getName();
        this.worldUUID = loc.getWorld().getUID().toString();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public AbstractPositionLog(List<Object> args) throws ConversionException
    {
        this.key = Integer.valueOf(args.get(0).toString());
        this.timestamp = (Timestamp)args.get(1);
        this.causeID = Integer.valueOf(args.get(2).toString());
        this.worldName = args.get(3).toString();
        this.worldUUID = args.get(4).toString();
        this.x = Integer.valueOf(args.get(5).toString());
        this.y = Integer.valueOf(args.get(6).toString());
        this.z = Integer.valueOf(args.get(7).toString());
    }
    
    public Location getLocation()
    {
        World world = CubeEngine.getServer().getWorld(UUID.fromString(worldUUID));
        if (world == null)
        {
            world = CubeEngine.getServer().getWorld(worldName);
        }
        return new Location(world, x, y, z);
    }

    public Integer getKey()
    {
        return key;
    }

    public void setKey(Integer key)
    {
        this.key = key;
    }

    public long getTimeStamp()
    {
        return this.timestamp.getTime();
    }

    public User getUser()
    {
        if (this.isCausedByPlayer())
        {
            return CubeEngine.getUserManager().getUser(causeID);
        }
        return null;
    }

    public boolean isCausedByPlayer()
    {
        return (causeID > 0);
    }
}