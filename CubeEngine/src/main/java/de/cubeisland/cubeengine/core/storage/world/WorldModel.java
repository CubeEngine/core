package de.cubeisland.cubeengine.core.storage.world;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;
import org.bukkit.World;

@SingleIntKeyEntity(tableName = "worlds", primaryKey = "key")
public class WorldModel implements Model<Integer>
{
    @Attribute(type = AttrType.INT)
    public int key = -1;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldUUID;

    public WorldModel()
    {
    }
    
    public WorldModel(World world)
    {
        this.worldName = world.getName();
        this.worldUUID = world.getUID().toString();
    }

    @Override
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
