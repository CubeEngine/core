package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;
import java.sql.Date;

@SingleIntKeyEntity(tableName = "orders", primaryKey = "id")
public class TestModel implements Model<Integer>
{
    @Attribute(type = AttrType.INT)
    public int id;
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
    public Integer getKey()
    {
        return this.id;
    }

    @Override
    public void setKey(Integer key)
    {
        this.id = key;
    }
}
