package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.util.Pair;

@TwoKeyEntity(tableName = "kitsgiven", firstPrimaryKey = "userId", secondPrimaryKey = "kitName",
              indices =
{
    @Index(value = FOREIGN_KEY, fields = "userId", f_table = "user", f_field = "key")
})
public class KitsGiven implements TwoKeyModel<Long, String>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Attribute(type = AttrType.VARCHAR, length = 50)
    public String kitName;
    @Attribute(type = AttrType.INT)
    public int amount;

    @Override
    public Pair<Long, String> getKey()
    {
        return new Pair<Long, String>(userId, kitName);
    }

    @Override
    public void setKey(Pair<Long, String> key)
    {
        this.userId = key.getLeft();
        this.kitName = key.getRight();
    }
}
