package de.cubeisland.cubeengine.core.persistence.testingdbstuff;

import de.cubeisland.cubeengine.core.persistence.*;
import de.cubeisland.cubeengine.core.persistence.Model;

/**
 *
 * @author Anselm Brehme
 */
public class RandomModel implements Model<Integer>
{

    @Attribute(name="testname",type=AttrType.VARCHAR)
    private String stringvalue = "Cookies";
    
    
    public Integer getKey()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
