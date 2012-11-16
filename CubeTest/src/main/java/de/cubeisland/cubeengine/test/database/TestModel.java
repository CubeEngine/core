package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import java.sql.Date;
import java.util.List;

@Entity(name = "Orders")
public class TestModel implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int id;
    @Attribute(type = AttrType.DATE)
    public Date orderDate;
    @Attribute(type = AttrType.DOUBLE)
    public double orderPrice;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String customer;

    @DatabaseConstructor
    public TestModel(List<Object> args)
    {
        this.id = Integer.valueOf(args.get(0).toString());
        this.orderDate = (Date)args.get(1);
        this.orderPrice = Double.valueOf(args.get(2).toString());
        this.customer = (String)args.get(3);
    }

    public TestModel(Date orderDate, double orderPrice, String customer)
    {
        this.id = -1;
        this.orderDate = orderDate;
        this.orderPrice = orderPrice;
        this.customer = customer;
    }

    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }
}
