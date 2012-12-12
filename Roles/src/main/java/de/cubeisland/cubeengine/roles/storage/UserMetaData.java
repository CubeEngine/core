package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.TripletKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TripletKeyEntity;
import de.cubeisland.cubeengine.core.util.Triplet;

@TripletKeyEntity(tableName = "userdata", firstPrimaryKey = "userId", secondPrimaryKey = "worldId", thirdPrimaryKey = "key")
public class UserMetaData implements TripletKeyModel<Integer, Integer, String>
{
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int userId;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "worlds", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String key;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String value;

    public UserMetaData(int userId, int worldId, String key, String value)
    {
        this.userId = userId;
        this.worldId = worldId;
        this.key = key;
        this.value = value;
    }

    @Override
    public Triplet<Integer, Integer, String> getKey()
    {
        return new Triplet<Integer, Integer, String>(userId, worldId, key);
    }

    @Override
    public void setKey(Triplet<Integer, Integer, String> key)
    {
        this.userId = key.getFirst();
        this.worldId = key.getSecond();
        this.key = key.getThird();
    }
}
