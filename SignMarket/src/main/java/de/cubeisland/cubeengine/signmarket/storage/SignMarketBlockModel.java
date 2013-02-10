package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketblocks",
indices = {
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "world", f_field = "key", f_table = "worlds", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.INDEX, fields = {"x","y","z"})
})
public class SignMarketBlockModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long world;
    @Attribute(type = AttrType.INT)
    public long x;
    @Attribute(type = AttrType.INT)
    public long y;
    @Attribute(type = AttrType.INT)
    public long z;

    @Override
    public Long getKey() {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    public SignMarketBlockModel() {
    }
}
