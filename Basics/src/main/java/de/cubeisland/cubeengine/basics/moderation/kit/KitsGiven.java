package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.ForeignKey;
import de.cubeisland.cubeengine.core.storage.database.PrimaryKey;

public class KitsGiven implements Model<Integer>
{
    @PrimaryKey
    @Attribute(type = AttrType.INT, unsigned = true)
    public int key;
    @ForeignKey(table = "user", field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int user;
    @Attribute(type = AttrType.VARCHAR, length = 50)//TODO limit name length in config
    public String kitName;
    @Attribute(type = AttrType.INT, unsigned = true)
    public int amount;

    @Override
    public Integer getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
