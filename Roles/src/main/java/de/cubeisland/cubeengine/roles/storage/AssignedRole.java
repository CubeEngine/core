package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;

@SingleIntKeyEntity(tableName = "roles", primaryKey = "key")
public class AssignedRole implements Model<Integer>
{
    @Attribute(type = AttrType.INT)
    public int key = -1;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int userId;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "worlds", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String roleName;

    public AssignedRole(int userId, int worldId, String roleName)
    {
        this.userId = userId;
        this.worldId = worldId;
        this.roleName = roleName;
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
