package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import java.sql.Date;

@SingleKeyEntity(tableName = "orders", primaryKey = "id", autoIncrement = true)
public class TestModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long id;
    @Attribute(type = AttrType.DATE)
    public Date orderDate;
    @Attribute(type = AttrType.DOUBLE)
    public double orderPrice;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String customer;

    public TestModel()
    {
    }

    public TestModel(Date orderDate, double orderPrice, String customer)
    {
        this.id = -1;
        this.orderDate = orderDate;
        this.orderPrice = orderPrice;
        this.customer = customer;
    }

    @Override
    public Long getKey()
    {
        return this.id;
    }

    @Override
    public void setKey(Long key)
    {
        this.id = key;
    }
}
