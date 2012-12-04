package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;

/**
 * Represents a table in database with a revision.
 */
@SingleIntKeyEntity(tableName = "tables", primaryKey = "key")
public class Table implements Model<Integer>
{
    @Attribute(type = AttrType.INT)
    public int key;
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
    public Integer getKey()
    {
        return key;
    }

    @Override
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
