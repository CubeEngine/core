package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import java.sql.Date;

/**
 *
 * @author Anselm Brehme
 */
@Entity(name = "Orders")
public class TestModel
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int id;
    @Attribute(name = "OrderDate", type = AttrType.VARCHAR, length = 16)
    public Date orderDate;
    @Attribute(name = "OrderPrice", type = AttrType.DOUBLE)
    public double orderPrice;
    @Attribute(name = "Customer", type = AttrType.VARCHAR, length = 16)
    public String customer;

    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }
}