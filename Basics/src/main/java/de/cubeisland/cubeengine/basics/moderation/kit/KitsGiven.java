package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.util.Pair;

@TwoKeyEntity(
tableName = "kitsgiven",
              firstPrimaryKey = "userId",
              secondPrimaryKey = "kitName")
public class KitsGiven implements TwoKeyModel<Integer, String>
{
    // @ForeignKey(table = "user", field = "key")
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT)
    public int    userId;
    @Attribute(type = AttrType.VARCHAR, length = 50)
    public String kitName;
    @Attribute(type = AttrType.INT)
    public int    amount;

    @Override
    public Pair<Integer, String> getKey()
    {
        return new Pair<Integer, String>(userId, kitName);
    }

    @Override
    public void setKey(Pair<Integer, String> key)
    {
        this.userId = key.getLeft();
        this.kitName = key.getRight();
    }
}
