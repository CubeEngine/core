package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

/**
 * Represents a table in database with a revision.
 */
@SingleKeyEntity(tableName = "tables", primaryKey = "key")
public class Table implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.VARCHAR, length = 32)
    public String table;
    @Attribute(type = AttrType.INT)
    public int revision;

    public Table()
    {
    }

    public Table(String table, Integer revision)
    {
        this.table = table;
        this.revision = revision;
    }

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
}
