package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.List;

/**
 * Represents a table in database with a revision.
 */
@Entity(name = "tables")
public class Table implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int          key;
    @Attribute(type = AttrType.VARCHAR, length = 32)
    public final String table;
    @Attribute(type = AttrType.INT, unsigned = true)
    public int          revision;

    @DatabaseConstructor
    public Table(List<Object> args)
    {
        try
        {
            this.key = Convert.fromObject(Integer.class, args.get(0));
            this.table = (String)args.get(1);
            this.revision = Convert.fromObject(Integer.class, args.get(2));
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Error while creating a User from Database");
        }
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
