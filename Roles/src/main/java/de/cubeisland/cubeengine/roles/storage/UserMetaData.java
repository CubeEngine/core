package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.TripletKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TripletKeyEntity;
import de.cubeisland.cubeengine.core.util.Triplet;

@TripletKeyEntity(tableName = "userdata", firstPrimaryKey = "userId", secondPrimaryKey = "worldId", thirdPrimaryKey = "key")
public class UserMetaData implements TripletKeyModel<Long, Long, String>
{
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public long   userId;
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "worlds", f_field = "key")
    @Attribute(type = AttrType.INT)
    public long   worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String key;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String value;

    public UserMetaData(long userId, long worldId, String key, String value)
    {
        this.userId = userId;
        this.worldId = worldId;
        this.key = key;
        this.value = value;
    }

    @Override
    public Triplet<Long, Long, String> getKey()
    {
        return new Triplet<Long, Long, String>(userId, worldId, key);
    }

    @Override
    public void setKey(Triplet<Long, Long, String> key)
    {
        this.userId = key.getFirst();
        this.worldId = key.getSecond();
        this.key = key.getThird();
    }
}
