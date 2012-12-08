package de.cubeisland.cubeengine.fly.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleIntKeyEntity(tableName = "fly", primaryKey = "key", autoIncrement = false)
public class FlyModel implements Model<Integer>
{
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int key;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean flying;

    public FlyModel(){}
    
    public FlyModel(User user)
    {
        this.key = user.getKey();
        this.flying = user.isFlying();
    }

    @Override
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
