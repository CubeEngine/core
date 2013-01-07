package de.cubeisland.cubeengine.conomy.account.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

@SingleKeyEntity(tableName = "accounts", primaryKey = "key", autoIncrement = true,
indices =
{
    @Index(value = FOREIGN_KEY, fields = "user_id", f_table = "user", f_field = "key")
})
public class AccountModel implements Model<Long>
{

    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long user_id;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String name;
    @Attribute(type = AttrType.VARCHAR, length = 64)
    public String currencyName;
    @Attribute(type = AttrType.BIGINT)
    public long value;

    @Override
    public Long getKey()
    {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    public AccountModel()
    {
    }

    public AccountModel(Long user_id, String name, String currencyName, long balance)
    {
        this.user_id = user_id;
        this.name = name;
        this.currencyName = currencyName;
        this.value = balance;
    }
}
