package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.sql.Date;
import java.util.List;

/**
 *
 * @author Anselm Brehme
 */
@Entity(name = "Orders")
public class TestModel implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int id;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public Date orderDate;
    @Attribute(type = AttrType.DOUBLE)
    public double orderPrice;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public String customer;

    @DatabaseConstructor
    public TestModel(List<Object> args)
    {
        try
        {
            this.id = Convert.fromObject(Integer.class, args.get(0));
            this.orderDate = Convert.fromObject(Date.class,args.get(1));
            this.orderPrice = Convert.fromObject(Double.class, args.get(2));
            this.customer = (String)args.get(3);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Error while converting Objects for Modelcreation");
        }
        
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