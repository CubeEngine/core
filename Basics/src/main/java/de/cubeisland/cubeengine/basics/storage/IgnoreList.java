package de.cubeisland.cubeengine.basics.storage;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;

@TwoKeyEntity(tableName = "ignorelist",
firstPrimaryKey = "key", secondPrimaryKey = "ignore",
indices =
{
    @Index(value = FOREIGN_KEY, fields = "key", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "ignore", f_table = "user", f_field = "key")
})
public class IgnoreList implements TwoKeyModel<Long, Long>
{

    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long ignore;

    IgnoreList(User user, User ignore)
    {
        this.key = user.key;
        this.ignore = ignore.key;
    }

    @Override
    public Pair<Long, Long> getKey()
    {
        return new Pair<Long, Long>(key, ignore);
    }

    @Override
    public void setKey(Pair<Long, Long> key)
    {
        this.key = key.getLeft();
        this.ignore = key.getRight();
    }
}
