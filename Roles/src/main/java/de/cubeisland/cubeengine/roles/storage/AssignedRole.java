package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.TripletKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TripletKeyEntity;
import de.cubeisland.cubeengine.core.util.Triplet;

@TripletKeyEntity(tableName = "roles", firstPrimaryKey = "userId", secondPrimaryKey = "worldId", thirdPrimaryKey = "roleName")
public class AssignedRole implements TripletKeyModel<Long, Long, String>
{
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "worlds", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public long worldId;
    @Attribute(type = AttrType.VARCHAR, length = 255)
    public String roleName;

    public AssignedRole(long userId, long worldId, String roleName)
    {
        this.userId = userId;
        this.worldId = worldId;
        this.roleName = roleName;
    }

    @Override
    public Triplet<Long, Long, String> getKey()
    {
        return new Triplet<Long, Long, String>(this.userId, this.worldId, this.roleName);
    }

    @Override
    public void setKey(Triplet<Long, Long, String> key)
    {
        this.userId = key.getFirst();
        this.worldId = key.getSecond();
        this.roleName = key.getThird();
    }
}
