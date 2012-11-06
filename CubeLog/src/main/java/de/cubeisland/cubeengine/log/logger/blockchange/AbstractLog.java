package de.cubeisland.cubeengine.log.logger.blockchange;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.Timestamp;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class AbstractLog implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp timestamp;
    @Attribute(type = AttrType.INT)
    public int causeID;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public World world; //TODO secondary key for this
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;
    
    public Location getLocation()
    {
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