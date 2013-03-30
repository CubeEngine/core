package de.cubeisland.cubeengine.core.storage.world;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import org.bukkit.World;

@SingleKeyEntity(tableName = "worlds", primaryKey = "key", autoIncrement = true)
public class WorldModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key = -1L;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldName;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String worldUUID;

    public WorldModel()
    {}

    public WorldModel(World world)
    {
        this.worldName = world.getName();
        this.worldUUID = world.getUID().toString();
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }
}
