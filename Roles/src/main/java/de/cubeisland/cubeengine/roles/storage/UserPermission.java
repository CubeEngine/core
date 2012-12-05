package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.util.Pair;

@TwoKeyEntity(tableName = "userperm", firstPrimaryKey = "userId", secondPrimaryKey = "perm")
public class UserPermission implements TwoKeyModel<Integer, String>
{
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int userId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String perm;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean isSet;

    @Override
    public Pair<Integer, String> getKey()
    {
        return new Pair<Integer, String>(this.userId, this.perm);
    }

    @Override
    public void setKey(Pair<Integer, String> key)
    {
        this.userId = key.getLeft();
        this.perm = key.getRight();
    }
}
