package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.util.Pair;

@TwoKeyEntity(tableName = "userdata", firstPrimaryKey = "userId", secondPrimaryKey = "key")
public class UserMetaData implements TwoKeyModel<Integer, String>
{
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int userId;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "worlds", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int world;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String key;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String value;

    @Override
    public Pair<Integer, String> getKey()
    {
        return new Pair<Integer, String>(this.userId, this.key);
    }

    @Override
    public void setKey(Pair<Integer, String> key)
    {
        this.userId = key.getLeft();
        this.key = key.getRight();
    }
}
