package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;


/**
 *
 * @author Anselm Brehme
 */
@Entity(name = "test")
public class TestModel
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public int id = -1;
    
    @Attribute(name = "string", type = AttrType.VARCHAR, length=16)
    public String stringvalue = "Cookies";
    
    @Attribute(name= "bool", type = AttrType.BOOLEAN)
    public boolean boolvalue = true;
    
    @Attribute(name= "time", type = AttrType.TIMESTAMP)
    public long timevalue = 0;
    
    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }
}