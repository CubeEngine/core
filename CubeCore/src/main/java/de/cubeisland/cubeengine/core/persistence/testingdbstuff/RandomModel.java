package de.cubeisland.cubeengine.core.persistence.testingdbstuff;

import de.cubeisland.cubeengine.core.persistence.AttrType;
import de.cubeisland.cubeengine.core.persistence.Attribute;
import de.cubeisland.cubeengine.core.persistence.Entity;
import de.cubeisland.cubeengine.core.persistence.Key;
import de.cubeisland.cubeengine.core.persistence.Model;

/**
 *
 * @author Anselm Brehme
 */
@Entity
public class RandomModel implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public int id = -1;
    
    @Attribute(name = "testname", type = AttrType.VARCHAR)
    public String stringvalue = "Cookies";
    
    
    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }
}
