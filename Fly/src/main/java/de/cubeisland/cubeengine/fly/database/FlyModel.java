package de.cubeisland.cubeengine.fly.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleKeyEntity(tableName = "fly", primaryKey = "key", autoIncrement = false,
                 indices =
{
    @Index(value = FOREIGN_KEY, fields = "key", f_table = "user", f_field = "key")
})
public class FlyModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean flying;

    public FlyModel()
    {
    }

    public FlyModel(User user)
    {
        this.key = user.getKey();
        this.flying = user.isFlying();
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
